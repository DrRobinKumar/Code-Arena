package com.codearena.service;

import com.codearena.dto.request.SubmitCodeRequest;
import com.codearena.dto.response.SubmissionDetailResponse;
import com.codearena.entity.DifficultyLevel;
import com.codearena.entity.Language;
import com.codearena.entity.Problem;
import com.codearena.entity.Submission;
import com.codearena.entity.TestCase;
import com.codearena.entity.User;
import com.codearena.entity.Verdict;
import com.codearena.exception.BadRequestException;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.judge.CodeExecutionResult;
import com.codearena.judge.CodeExecutionService;
import com.codearena.mapper.SubmissionMapper;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.SubmissionRepository;
import com.codearena.repository.UserRepository;
import com.codearena.serviceImpl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceImplTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CodeExecutionService codeExecutionService;
    @Mock
    private SubmissionMapper submissionMapper;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Problem problem;
    private User user;
    private SubmitCodeRequest request;

    @BeforeEach
    void setUp() {
        TestCase visible = TestCase.builder().input("1 2").expectedOutput("3").hidden(false).build();
        TestCase hidden = TestCase.builder().input("5 5").expectedOutput("10").hidden(true).build();

        problem = Problem.builder()
                .title("Add Two Numbers")
                .slug("add-two-numbers")
                .difficulty(DifficultyLevel.EASY)
                .description("desc")
                .timeLimitMs(2000)
                .memoryLimitKb(262144)
                .build();
        problem.setId(1L);
        problem.replaceTestCases(List.of(visible, hidden));

        user = User.builder().username("john_doe").email("john@example.com").password("hashed").fullName("John").build();
        user.setId(1L);

        request = new SubmitCodeRequest("add-two-numbers", Language.JAVA, "public class Main {}");
    }

    /** Shared by every test that exercises the full submitCode save+map path. */
    private void stubSaveAndMap() {
        when(submissionMapper.toDetailResponse(any(Submission.class)))
                .thenAnswer(inv -> SubmissionDetailResponse.builder()
                        .verdict(((Submission) inv.getArgument(0)).getVerdict())
                        .testCasesPassed(((Submission) inv.getArgument(0)).getTestCasesPassed())
                        .testCasesTotal(((Submission) inv.getArgument(0)).getTestCasesTotal())
                        .build());
        when(submissionRepository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void submitCode_shouldReturnAccepted_whenAllTestCasesPass() {
        stubSaveAndMap();
        when(problemRepository.findBySlug("add-two-numbers")).thenReturn(Optional.of(problem));
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(codeExecutionService.executeBatch(anyList())).thenReturn(List.of(
                CodeExecutionResult.builder().verdict(Verdict.ACCEPTED).executionTimeMs(10L).memoryKb(1000L).build(),
                CodeExecutionResult.builder().verdict(Verdict.ACCEPTED).executionTimeMs(15L).memoryKb(1200L).build()
        ));

        SubmissionDetailResponse response = submissionService.submitCode(request, "john_doe");

        assertThat(response.getVerdict()).isEqualTo(Verdict.ACCEPTED);
        assertThat(response.getTestCasesPassed()).isEqualTo(2);
        assertThat(response.getTestCasesTotal()).isEqualTo(2);
    }

    @Test
    void submitCode_shouldReturnWrongAnswer_whenOneTestCaseFailsAndNoneAreWorse() {
        stubSaveAndMap();
        when(problemRepository.findBySlug("add-two-numbers")).thenReturn(Optional.of(problem));
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(codeExecutionService.executeBatch(anyList())).thenReturn(List.of(
                CodeExecutionResult.builder().verdict(Verdict.ACCEPTED).executionTimeMs(10L).memoryKb(1000L).build(),
                CodeExecutionResult.builder().verdict(Verdict.WRONG_ANSWER).executionTimeMs(12L).memoryKb(1100L).build()
        ));

        SubmissionDetailResponse response = submissionService.submitCode(request, "john_doe");

        assertThat(response.getVerdict()).isEqualTo(Verdict.WRONG_ANSWER);
        assertThat(response.getTestCasesPassed()).isEqualTo(1);
    }

    @Test
    void submitCode_shouldPrioritizeCompilationError_overOtherFailures() {
        stubSaveAndMap();
        when(problemRepository.findBySlug("add-two-numbers")).thenReturn(Optional.of(problem));
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(codeExecutionService.executeBatch(anyList())).thenReturn(List.of(
                CodeExecutionResult.builder().verdict(Verdict.COMPILATION_ERROR).compileOutput("syntax error").build(),
                CodeExecutionResult.builder().verdict(Verdict.WRONG_ANSWER).build()
        ));

        SubmissionDetailResponse response = submissionService.submitCode(request, "john_doe");

        assertThat(response.getVerdict()).isEqualTo(Verdict.COMPILATION_ERROR);
    }

    @Test
    void submitCode_shouldThrowResourceNotFoundException_whenProblemMissing() {
        when(problemRepository.findBySlug("add-two-numbers")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionService.submitCode(request, "john_doe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void submitCode_shouldThrowBadRequestException_whenProblemHasNoTestCases() {
        Problem emptyProblem = Problem.builder()
                .title("Empty").slug("empty").difficulty(DifficultyLevel.EASY).description("d")
                .timeLimitMs(2000).memoryLimitKb(262144).build();
        when(problemRepository.findBySlug("add-two-numbers")).thenReturn(Optional.of(emptyProblem));
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> submissionService.submitCode(request, "john_doe"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getOwnSubmission_shouldThrowResourceNotFoundException_whenSubmissionBelongsToSomeoneElse() {
        User owner = User.builder().username("someone_else").build();
        Submission submission = Submission.builder().user(owner).problem(problem).build();
        submission.setId(1L);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> submissionService.getOwnSubmission(1L, "john_doe"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOwnSubmission_shouldReturnDetail_whenRequesterIsOwner() {
        Submission submission = Submission.builder().user(user).problem(problem).verdict(Verdict.ACCEPTED).build();
        submission.setId(1L);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(submissionMapper.toDetailResponse(any(Submission.class)))
                .thenReturn(SubmissionDetailResponse.builder().verdict(Verdict.ACCEPTED).build());
        ArgumentCaptor<Submission> captor = ArgumentCaptor.forClass(Submission.class);

        submissionService.getOwnSubmission(1L, "john_doe");

        org.mockito.Mockito.verify(submissionMapper).toDetailResponse(captor.capture());
        assertThat(captor.getValue().getUser().getUsername()).isEqualTo("john_doe");
    }
}
