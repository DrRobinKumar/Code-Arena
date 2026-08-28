package com.codearena.controller;

import com.codearena.dto.response.ApiResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.SubmissionDetailResponse;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.entity.Language;
import com.codearena.entity.Verdict;
import com.codearena.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/submissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Submissions", description = "Unrestricted submission visibility across all users, admin only")
public class AdminSubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/{id}")
    @Operation(summary = "Get any submission in full detail, regardless of owner")
    public ResponseEntity<ApiResponse<SubmissionDetailResponse>> getSubmission(@PathVariable Long id) {
        SubmissionDetailResponse response = submissionService.getSubmissionForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Search/filter/paginate submissions across all users")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> searchSubmissions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) Verdict verdict,
            @RequestParam(required = false) Language language,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        PageResponse<SubmissionResponse> response = submissionService.getAllSubmissions(
                userId, problemId, verdict, language, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
