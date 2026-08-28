package com.codearena.controller;

import com.codearena.dto.response.ApiResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.ProblemResponse;
import com.codearena.dto.response.ProblemSummaryResponse;
import com.codearena.entity.DifficultyLevel;
import com.codearena.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
@Tag(name = "Problems", description = "Public problem browsing: view, search, filter, paginate, sort")
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    @Operation(summary = "Search/filter/paginate problems",
            description = "Example: /api/v1/problems?search=sum&difficulty=EASY&tags=array,hash-table&page=0&size=20&sort=title,asc")
    public ResponseEntity<ApiResponse<PageResponse<ProblemSummaryResponse>>> searchProblems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @Parameter(description = "Comma-separated tag names; matches problems having ANY of them")
            @RequestParam(required = false) List<String> tags,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        PageResponse<ProblemSummaryResponse> response =
                problemService.searchProblems(search, difficulty, tags, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full problem detail by slug (public — no editorial or hidden test cases)")
    public ResponseEntity<ApiResponse<ProblemResponse>> getBySlug(@PathVariable String slug) {
        ProblemResponse response = problemService.getProblemBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
