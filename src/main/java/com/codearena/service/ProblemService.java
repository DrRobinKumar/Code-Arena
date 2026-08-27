package com.codearena.service;

import com.codearena.dto.request.ProblemCreateRequest;
import com.codearena.dto.request.ProblemUpdateRequest;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.ProblemAdminResponse;
import com.codearena.dto.response.ProblemResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.entity.DifficultyLevel;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProblemService {

    ProblemAdminResponse createProblem(ProblemCreateRequest request);

    ProblemAdminResponse updateProblem(Long id, ProblemUpdateRequest request);

    void deleteProblem(Long id);

    ProblemAdminResponse getProblemForAdmin(Long id);

    /** Public-facing detail lookup by slug — excludes editorial and hidden test cases. */
    ProblemResponse getProblemBySlug(String slug);

    /** Public search/filter/paginate/sort over problem summaries. */
    PageResponse<ProblemSummaryResponse> searchProblems(String search, DifficultyLevel difficulty,
                                                         List<String> tags, Pageable pageable);
}
