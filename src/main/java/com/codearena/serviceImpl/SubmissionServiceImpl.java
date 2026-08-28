package com.codearena.serviceImpl;

import com.codearena.dto.request.RunCodeRequest;
import com.codearena.dto.request.SubmitCodeRequest;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.RunCodeResponse;
import com.codearena.dto.response.SubmissionDetailResponse;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.entity.Language;
import com.codearena.entity.Problem;
import com.codearena.entity.Submission;
import com.codearena.entity.TestCase;
import com.codearena.entity.User;
import com.codearena.entity.Verdict;
import com.codearena.exception.BadRequestException;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.judge.CodeExecutionRequest;
import com.codearena.judge.CodeExecutionResult;
import com.codearena.judge.CodeExecutionService;
import com.codearena.judge.JudgeDefaults;
import com.codearena.mapper.SubmissionMapper;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import com.codearena.repository.specification.SubmissionSpecification;
import com.codearena.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionServiceImpl implements SubmissionService {

    /**
     * Worst-first priority used to reduce many per-test-case verdicts to one
     * overall submission verdict. ACCEPTED is intentionally absent — it's
     * the fallback when nothing worse is found among the results.
     */
    private static final List<Verdict> VERDICT_PRIORITY = List.of(
            Verdict.COMPILATION_ERROR,
            Verdict.INTERNAL_ERROR,
            Verdict.RUNTIME_ERROR,
            Verdict.TIME_LIMIT_EXCEEDED,
            Verdict.MEMORY_LIMIT_EXCEEDED,
            Verdict.WRONG_ANSWER
    );

    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final CodeExecutionService codeExecutionService;
    private final SubmissionMapper submissionMapper;

    @Override
    public RunCodeResponse runCode(RunCodeRequest request) {
        int timeLimitMs = JudgeDefaults.DEFAULT_TIME_LIMIT_MS;
        int memoryLimitKb = JudgeDefaults.DEFAULT_MEMORY_LIMIT_KB;

        if (request.getProblemSlug() != null && !request.getProblemSlug().isBlank()) {
            Problem problem = problemRepository.findBySlug(request.getProblemSlug())
                    .orElseThrow(() -> ResourceNotFoundException.of("Problem", "slug", request.getProblemSlug()));
            timeLimitMs = problem.getTimeLimitMs();
            memoryLimitKb = problem.getMemoryLimitKb();
        }

        CodeExecutionRequest execRequest = CodeExecutionRequest.builder()
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .stdin(request.getStdin())
                .timeLimitMs(timeLimitMs)
                .memoryLimitKb(memoryLimitKb)
                .build();

        CodeExecutionResult result = codeExecutionService.execute(execRequest);

        return RunCodeResponse.builder()
                .status(result.getVerdict())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .compileOutput(result.getCompileOutput())
                .executionTimeMs(result.getExecutionTimeMs())
                .memoryKb(result.getMemoryKb())
                .build();
    }

    @Override
    @Transactional
    public SubmissionDetailResponse submitCode(SubmitCodeRequest request, String username) {
        Problem problem = problemRepository.findBySlug(request.getProblemSlug())
                .orElseThrow(() -> ResourceNotFoundException.of("Problem", "slug", request.getProblemSlug()));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "username", username));

        List<TestCase> testCases = problem.getTestCases();
        if (testCases.isEmpty()) {
            throw new BadRequestException("This problem has no test cases configured yet");
        }

        List<CodeExecutionRequest> execRequests = testCases.stream()
                .map(tc -> CodeExecutionRequest.builder()
                        .language(request.getLanguage())
                        .sourceCode(request.getSourceCode())
                        .stdin(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .timeLimitMs(problem.getTimeLimitMs())
                        .memoryLimitKb(problem.getMemoryLimitKb())
                        .build())
                .toList();

        List<CodeExecutionResult> results = codeExecutionService.executeBatch(execRequests);

        Submission submission = buildSubmission(user, problem, request, results);
        Submission saved = submissionRepository.save(submission);
        return submissionMapper.toDetailResponse(saved);
    }

    private Submission buildSubmission(User user, Problem problem, SubmitCodeRequest request,
                                        List<CodeExecutionResult> results) {
        Verdict overallVerdict = VERDICT_PRIORITY.stream()
                .filter(v -> results.stream().anyMatch(r -> r.getVerdict() == v))
                .findFirst()
                .orElse(Verdict.ACCEPTED);

        int passed = (int) results.stream().filter(r -> r.getVerdict() == Verdict.ACCEPTED).count();

        long maxTimeMs = results.stream()
                .map(CodeExecutionResult::getExecutionTimeMs)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .max().orElse(0L);

        long maxMemoryKb = results.stream()
                .map(CodeExecutionResult::getMemoryKb)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .max().orElse(0L);

        String compileOutput = results.stream()
                .filter(r -> r.getVerdict() == Verdict.COMPILATION_ERROR)
                .map(CodeExecutionResult::getCompileOutput)
                .findFirst()
                .orElse(null);

        String errorMessage = results.stream()
                .filter(r -> r.getVerdict() != Verdict.ACCEPTED)
                .map(CodeExecutionResult::getStderr)
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .orElse(null);

        return Submission.builder()
                .user(user)
                .problem(problem)
                .language(request.getLanguage())
                .sourceCode(request.getSourceCode())
                .verdict(overallVerdict)
                .runtimeMs(maxTimeMs)
                .memoryKb(maxMemoryKb)
                .testCasesPassed(passed)
                .testCasesTotal(results.size())
                .compileOutput(compileOutput)
                .errorMessage(errorMessage)
                .build();
    }

    @Override
    public SubmissionDetailResponse getOwnSubmission(Long id, String username) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Submission", "id", id));

        // A submission that exists but belongs to someone else is reported
        // identically to "doesn't exist" — deliberately not a 403, so we
        // don't confirm to a caller that a given submission ID belongs to
        // another user.
        if (!submission.getUser().getUsername().equals(username)) {
            throw ResourceNotFoundException.of("Submission", "id", id);
        }
        return submissionMapper.toDetailResponse(submission);
    }

    @Override
    public SubmissionDetailResponse getSubmissionForAdmin(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Submission", "id", id));
        return submissionMapper.toDetailResponse(submission);
    }

    @Override
    public PageResponse<SubmissionResponse> getMySubmissions(String username, Long problemId, Verdict verdict,
                                                              Language language, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "username", username));

        Specification<Submission> spec = SubmissionSpecification.combine(
                SubmissionSpecification.byUserId(user.getId()),
                SubmissionSpecification.byProblemId(problemId),
                SubmissionSpecification.byVerdict(verdict),
                SubmissionSpecification.byLanguage(language));

        Page<SubmissionResponse> page = submissionRepository.findAll(spec, pageable).map(submissionMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    public PageResponse<SubmissionResponse> getAllSubmissions(Long userId, Long problemId, Verdict verdict,
                                                               Language language, Pageable pageable) {
        Specification<Submission> spec = SubmissionSpecification.combine(
                SubmissionSpecification.byUserId(userId),
                SubmissionSpecification.byProblemId(problemId),
                SubmissionSpecification.byVerdict(verdict),
                SubmissionSpecification.byLanguage(language));

        Page<SubmissionResponse> page = submissionRepository.findAll(spec, pageable).map(submissionMapper::toResponse);
        return PageResponse.from(page);
    }
}
