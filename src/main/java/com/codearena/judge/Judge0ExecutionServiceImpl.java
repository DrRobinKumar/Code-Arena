package com.codearena.judge;

import com.codearena.entity.Verdict;
import com.codearena.exception.CodeExecutionException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Talks to a Judge0 CE-compatible REST API (self-hosted or RapidAPI-hosted
 * — both use the same wire format; RapidAPI additionally requires the two
 * X-RapidAPI-* headers, added only when configured).
 *
 * Always submits via /submissions/batch — even a single Run is one batch
 * of size one — and always submits-then-polls (never relies on wait=true
 * being honored), since that behavior isn't guaranteed identical across
 * every Judge0 deployment. This is the ONLY class in the codebase that
 * knows Judge0's wire format; everything else depends on
 * {@link CodeExecutionService}.
 */
@Slf4j
@Service
public class Judge0ExecutionServiceImpl implements CodeExecutionService {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String rapidApiKey;
    private final String rapidApiHost;
    private final long pollIntervalMs;
    private final int maxPollAttempts;

    public Judge0ExecutionServiceImpl(
            RestTemplate judge0RestTemplate,
            @Value("${app.judge0.base-url}") String baseUrl,
            @Value("${app.judge0.rapidapi-key:}") String rapidApiKey,
            @Value("${app.judge0.rapidapi-host:}") String rapidApiHost,
            @Value("${app.judge0.poll-interval-ms:1000}") long pollIntervalMs,
            @Value("${app.judge0.max-poll-attempts:10}") int maxPollAttempts) {
        this.restTemplate = judge0RestTemplate;
        this.baseUrl = baseUrl;
        this.rapidApiKey = rapidApiKey;
        this.rapidApiHost = rapidApiHost;
        this.pollIntervalMs = pollIntervalMs;
        this.maxPollAttempts = maxPollAttempts;
    }

    @Override
    public List<CodeExecutionResult> executeBatch(List<CodeExecutionRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<String> tokens = submitBatch(requests);
        List<Judge0SubmissionResult> results = pollUntilTerminal(tokens);

        List<CodeExecutionResult> mapped = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            mapped.add(toCodeExecutionResult(results.get(i), requests.get(i)));
        }
        return mapped;
    }

    private List<String> submitBatch(List<CodeExecutionRequest> requests) {
        List<Judge0SubmissionPayload> payloads = requests.stream()
                .map(Judge0SubmissionPayload::from)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("submissions", payloads);

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/submissions/batch")
                .queryParam("base64_encoded", "true")
                .toUriString();

        try {
            SubmitBatchResponseItem[] response = restTemplate.postForObject(
                    url, new HttpEntity<>(body, buildHeaders()), SubmitBatchResponseItem[].class);
            if (response == null) {
                throw new CodeExecutionException("Judge0 returned an empty response for batch submission");
            }
            return List.of(response).stream().map(SubmitBatchResponseItem::getToken).toList();
        } catch (RestClientException ex) {
            throw new CodeExecutionException("Failed to submit code to the judge: " + ex.getMessage(), ex);
        }
    }

    private List<Judge0SubmissionResult> pollUntilTerminal(List<String> tokens) {
        String tokenCsv = String.join(",", tokens);
        List<Judge0SubmissionResult> latest = fetchBatchResults(tokenCsv);

        int attempts = 0;
        while (attempts < maxPollAttempts && !allTerminal(latest)) {
            sleep(pollIntervalMs);
            latest = fetchBatchResults(tokenCsv);
            attempts++;
        }

        if (!allTerminal(latest)) {
            throw new CodeExecutionException(
                    "Judge did not return a final result within the poll budget (%d attempts)".formatted(maxPollAttempts));
        }
        return latest;
    }

    private List<Judge0SubmissionResult> fetchBatchResults(String tokenCsv) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/submissions/batch")
                .queryParam("tokens", tokenCsv)
                .queryParam("base64_encoded", "true")
                .queryParam("fields", "token,stdout,stderr,compile_output,message,time,memory,status")
                .toUriString();

        try {
            BatchResultResponse response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), BatchResultResponse.class).getBody();
            if (response == null || response.getSubmissions() == null) {
                throw new CodeExecutionException("Judge0 returned an empty response while polling for results");
            }
            return response.getSubmissions();
        } catch (RestClientException ex) {
            throw new CodeExecutionException("Failed to fetch results from the judge: " + ex.getMessage(), ex);
        }
    }

    private boolean allTerminal(List<Judge0SubmissionResult> results) {
        return results.stream().allMatch(Judge0SubmissionResult::isTerminal);
    }

    private CodeExecutionResult toCodeExecutionResult(Judge0SubmissionResult result, CodeExecutionRequest originalRequest) {
        Verdict verdict = Judge0StatusMapper.map(result.getStatus().getId());
        Long memoryKb = result.getMemory() == null ? null : result.getMemory().longValue();

        // Judge0 doesn't reliably surface a dedicated "Memory Limit Exceeded"
        // status — an OOM kill typically shows up as a Runtime Error. We
        // decide MLE ourselves by comparing reported usage against the
        // limit we asked Judge0 to enforce, and this check takes priority
        // over whatever Judge0 concluded.
        if (memoryKb != null && memoryKb > originalRequest.getMemoryLimitKb()) {
            verdict = Verdict.MEMORY_LIMIT_EXCEEDED;
        }

        return CodeExecutionResult.builder()
                .verdict(verdict)
                .stdout(result.decodedStdout())
                .stderr(result.decodedStderr())
                .compileOutput(result.decodedCompileOutput())
                .executionTimeMs(result.timeInMillis())
                .memoryKb(memoryKb)
                .build();
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (rapidApiKey != null && !rapidApiKey.isBlank()) {
            headers.set("X-RapidAPI-Key", rapidApiKey);
            headers.set("X-RapidAPI-Host", rapidApiHost);
        }
        return headers;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CodeExecutionException("Interrupted while waiting for judge results", e);
        }
    }

    @Getter
    @Setter
    private static class SubmitBatchResponseItem {
        private String token;
    }

    @Getter
    @Setter
    private static class BatchResultResponse {
        private List<Judge0SubmissionResult> submissions;
    }
}
