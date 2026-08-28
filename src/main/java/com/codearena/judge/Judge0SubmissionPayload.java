package com.codearena.judge;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Maps 1:1 onto a single element of Judge0's POST /submissions/batch body.
 * We always send base64_encoded=true (see Judge0ExecutionServiceImpl), so
 * every text field here is base64 — encoding happens once, in the factory
 * method, so callers never have to remember to do it themselves.
 */
@Getter
@Setter
public class Judge0SubmissionPayload {

    @JsonProperty("language_id")
    private int languageId;

    @JsonProperty("source_code")
    private String sourceCode;

    private String stdin;

    @JsonProperty("expected_output")
    private String expectedOutput;

    /** Judge0 wants seconds (may be fractional), we track milliseconds internally. */
    @JsonProperty("cpu_time_limit")
    private double cpuTimeLimit;

    /** Judge0 wants kilobytes, matching our own Problem.memoryLimitKb unit. */
    @JsonProperty("memory_limit")
    private int memoryLimit;

    public static Judge0SubmissionPayload from(CodeExecutionRequest request) {
        Judge0SubmissionPayload payload = new Judge0SubmissionPayload();
        payload.setLanguageId(request.getLanguage().getJudge0LanguageId());
        payload.setSourceCode(encode(request.getSourceCode()));
        payload.setStdin(encode(request.getStdin()));
        payload.setExpectedOutput(encode(request.getExpectedOutput()));
        payload.setCpuTimeLimit(request.getTimeLimitMs() / 1000.0);
        payload.setMemoryLimit(request.getMemoryLimitKb());
        return payload;
    }

    private static String encode(String value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
