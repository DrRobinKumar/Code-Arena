package com.codearena.service;

import com.codearena.dto.request.ProblemCreateRequest;
import com.codearena.dto.request.ProblemUpdateRequest;
import com.codearena.dto.request.TestCaseRequest;
import com.codearena.dto.response.ProblemAdminResponse;
import com.codearena.entity.DifficultyLevel;
import com.codearena.entity.Problem;
import com.codearena.entity.Tag;
import com.codearena.entity.TestCase;
import com.codearena.exception.DuplicateResourceException;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.mapper.ProblemMapper;
import com.codearena.mapper.TestCaseMapper;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.TagRepository;
import com.codearena.serviceImpl.ProblemServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProblemServiceImplTest {

    @Mock
    private ProblemRepository problemRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private ProblemMapper problemMapper;
    @Mock
    private TestCaseMapper testCaseMapper;

    @InjectMocks
    private ProblemServiceImpl problemService;

    private ProblemCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new ProblemCreateRequest();
        createRequest.setTitle("Two Sum");
        createRequest.setDifficulty(DifficultyLevel.EASY);
        createRequest.setDescription("Find two numbers that add up to target.");
        createRequest.setTimeLimitMs(2000);
        createRequest.setMemoryLimitKb(262144);
        createRequest.setTags(List.of("array", "hash-table"));
        createRequest.setTestCases(List.of(new TestCaseRequest("1 2 3", "5", false)));
    }

    @Test
    void createProblem_shouldGenerateSlugFromTitle_andResolveTags() {
        when(problemRepository.findBySlug("two-sum")).thenReturn(Optional.empty());
        when(tagRepository.findByNameIgnoreCase("array")).thenReturn(Optional.empty());
        when(tagRepository.findByNameIgnoreCase("hash-table")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(testCaseMapper.toEntity(any())).thenReturn(new TestCase());
        when(problemRepository.save(any(Problem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(problemMapper.toAdminResponse(any(Problem.class)))
                .thenReturn(ProblemAdminResponse.builder().title("Two Sum").slug("two-sum").build());

        ProblemAdminResponse response = problemService.createProblem(createRequest);

        assertThat(response.getSlug()).isEqualTo("two-sum");

        ArgumentCaptor<Problem> captor = ArgumentCaptor.forClass(Problem.class);
        verify(problemRepository).save(captor.capture());
        Problem saved = captor.getValue();
        assertThat(saved.getSlug()).isEqualTo("two-sum");
        assertThat(saved.getProblemTags()).hasSize(2);
        assertThat(saved.getTestCases()).hasSize(1);
    }

    @Test
    void createProblem_shouldAppendSuffix_whenSlugAlreadyTaken() {
        when(problemRepository.findBySlug("two-sum")).thenReturn(Optional.of(new Problem()));
        when(problemRepository.findBySlug("two-sum-2")).thenReturn(Optional.empty());
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(testCaseMapper.toEntity(any())).thenReturn(new TestCase());
        when(problemRepository.save(any(Problem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(problemMapper.toAdminResponse(any(Problem.class)))
                .thenReturn(ProblemAdminResponse.builder().slug("two-sum-2").build());

        ProblemAdminResponse response = problemService.createProblem(createRequest);

        assertThat(response.getSlug()).isEqualTo("two-sum-2");
    }

    @Test
    void updateProblem_shouldThrowResourceNotFoundException_whenProblemMissing() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.updateProblem(99L, new ProblemUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProblem_shouldThrowDuplicateResourceException_whenExplicitSlugConflictsWithAnotherProblem() {
        Problem existing = Problem.builder().title("Two Sum").slug("two-sum").difficulty(DifficultyLevel.EASY).build();
        existing.setId(1L);
        Problem other = Problem.builder().title("Three Sum").slug("three-sum").difficulty(DifficultyLevel.MEDIUM).build();
        other.setId(2L);

        ProblemUpdateRequest updateRequest = new ProblemUpdateRequest();
        updateRequest.setTitle("Two Sum");
        updateRequest.setSlug("three-sum");
        updateRequest.setDifficulty(DifficultyLevel.EASY);
        updateRequest.setDescription("desc");
        updateRequest.setTimeLimitMs(2000);
        updateRequest.setMemoryLimitKb(262144);
        updateRequest.setTestCases(List.of(new TestCaseRequest("in", "out", false)));

        when(problemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(problemRepository.findBySlug("three-sum")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> problemService.updateProblem(1L, updateRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(problemRepository, never()).save(any());
    }

    @Test
    void deleteProblem_shouldThrowResourceNotFoundException_whenMissing() {
        when(problemRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> problemService.deleteProblem(42L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(problemRepository, never()).deleteById(any());
    }

    @Test
    void deleteProblem_shouldDelete_whenExists() {
        when(problemRepository.existsById(1L)).thenReturn(true);

        problemService.deleteProblem(1L);

        verify(problemRepository, times(1)).deleteById(1L);
    }

    @Test
    void getProblemBySlug_shouldThrowResourceNotFoundException_whenMissing() {
        when(problemRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.getProblemBySlug("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
