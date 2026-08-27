package com.codearena.serviceImpl;

import com.codearena.dto.request.ExampleRequest;
import com.codearena.dto.request.ProblemCreateRequest;
import com.codearena.dto.request.ProblemUpdateRequest;
import com.codearena.dto.request.TestCaseRequest;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.ProblemAdminResponse;
import com.codearena.dto.response.ProblemResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.entity.DifficultyLevel;
import com.codearena.entity.Problem;
import com.codearena.entity.ProblemExample;
import com.codearena.entity.ProblemTag;
import com.codearena.entity.Tag;
import com.codearena.entity.TestCase;
import com.codearena.exception.DuplicateResourceException;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.mapper.ProblemMapper;
import com.codearena.mapper.TestCaseMapper;
import com.codearena.repository.ProblemRepository;
import com.codearena.repository.TagRepository;
import com.codearena.repository.specification.ProblemSpecification;
import com.codearena.service.ProblemService;
import com.codearena.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final TagRepository tagRepository;
    private final ProblemMapper problemMapper;
    private final TestCaseMapper testCaseMapper;

    @Override
    @Transactional
    public ProblemAdminResponse createProblem(ProblemCreateRequest request) {
        String slug = generateUniqueSlug(
                (request.getSlug() == null || request.getSlug().isBlank()) ? request.getTitle() : request.getSlug(),
                null);

        Problem problem = Problem.builder()
                .title(request.getTitle())
                .slug(slug)
                .difficulty(request.getDifficulty())
                .description(request.getDescription())
                .constraints(request.getConstraints())
                .inputFormat(request.getInputFormat())
                .outputFormat(request.getOutputFormat())
                .editorial(request.getEditorial())
                .timeLimitMs(request.getTimeLimitMs())
                .memoryLimitKb(request.getMemoryLimitKb())
                .hints(request.getHints() == null ? new ArrayList<>() : new ArrayList<>(request.getHints()))
                .examples(toExampleEntities(request.getExamples()))
                .build();

        problem.replaceProblemTags(toProblemTags(request.getTags()));
        problem.replaceTestCases(toTestCaseEntities(request.getTestCases()));

        Problem saved = problemRepository.save(problem);
        return problemMapper.toAdminResponse(saved);
    }

    @Override
    @Transactional
    public ProblemAdminResponse updateProblem(Long id, ProblemUpdateRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Problem", "id", id));

        String resolvedSlug = resolveSlugForUpdate(problem, request);

        problem.setTitle(request.getTitle());
        problem.setSlug(resolvedSlug);
        problem.setDifficulty(request.getDifficulty());
        problem.setDescription(request.getDescription());
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setEditorial(request.getEditorial());
        problem.setTimeLimitMs(request.getTimeLimitMs());
        problem.setMemoryLimitKb(request.getMemoryLimitKb());
        problem.setHints(request.getHints() == null ? new ArrayList<>() : new ArrayList<>(request.getHints()));
        problem.setExamples(toExampleEntities(request.getExamples()));

        problem.replaceProblemTags(toProblemTags(request.getTags()));
        problem.replaceTestCases(toTestCaseEntities(request.getTestCases()));

        Problem saved = problemRepository.save(problem);
        return problemMapper.toAdminResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Problem", "id", id);
        }
        problemRepository.deleteById(id);
    }

    @Override
    public ProblemAdminResponse getProblemForAdmin(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Problem", "id", id));
        return problemMapper.toAdminResponse(problem);
    }

    @Override
    public ProblemResponse getProblemBySlug(String slug) {
        Problem problem = problemRepository.findBySlug(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Problem", "slug", slug));
        return problemMapper.toPublicResponse(problem);
    }

    @Override
    public PageResponse<ProblemSummaryResponse> searchProblems(String search, DifficultyLevel difficulty,
                                                                List<String> tags, Pageable pageable) {
        Specification<Problem> spec = ProblemSpecification.combine(
                ProblemSpecification.titleContains(search),
                ProblemSpecification.hasDifficulty(difficulty),
                ProblemSpecification.hasAnyTag(tags));

        Page<ProblemSummaryResponse> page = problemRepository.findAll(spec, pageable)
                .map(problemMapper::toSummary);

        return PageResponse.from(page);
    }

    // --- helpers -----------------------------------------------------------

    private String generateUniqueSlug(String source, Long excludeProblemId) {
        String base = SlugUtil.toSlug(source);
        String candidate = base;
        int suffix = 2;
        while (slugTaken(candidate, excludeProblemId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean slugTaken(String candidate, Long excludeProblemId) {
        return problemRepository.findBySlug(candidate)
                .map(existing -> excludeProblemId == null || !existing.getId().equals(excludeProblemId))
                .orElse(false);
    }

    /**
     * Update-time slug resolution: an explicitly provided slug is treated as
     * an intentional admin decision and must be unique on its own — we fail
     * loudly on conflict rather than silently altering it. A blank slug
     * falls back to the existing one, or is regenerated (with an
     * auto-uniqueness suffix) only when the title actually changed.
     */
    private String resolveSlugForUpdate(Problem problem, ProblemUpdateRequest request) {
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String normalized = SlugUtil.toSlug(request.getSlug());
            if (normalized.equals(problem.getSlug())) {
                return normalized;
            }
            if (slugTaken(normalized, problem.getId())) {
                throw new DuplicateResourceException("Slug '" + normalized + "' is already in use");
            }
            return normalized;
        }

        if (!request.getTitle().equals(problem.getTitle())) {
            return generateUniqueSlug(request.getTitle(), problem.getId());
        }

        return problem.getSlug();
    }

    private List<ProblemExample> toExampleEntities(List<ExampleRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }
        return requests.stream()
                .map(r -> new ProblemExample(r.getInput(), r.getOutput(), r.getExplanation()))
                .toList();
    }

    private List<TestCase> toTestCaseEntities(List<TestCaseRequest> requests) {
        return requests.stream()
                .map(testCaseMapper::toEntity)
                .toList();
    }

    /** Finds each tag by name (case-insensitive), auto-creating any that don't exist yet. */
    private List<ProblemTag> toProblemTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        return tagNames.stream()
                .map(this::findOrCreateTag)
                .map(tag -> ProblemTag.builder().tag(tag).build())
                .toList();
    }

    private Tag findOrCreateTag(String name) {
        return tagRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name.trim()).build()));
    }
}
