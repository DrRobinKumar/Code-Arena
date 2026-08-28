package com.codearena.controller;

import com.codearena.dto.request.SubmitCodeRequest;
import com.codearena.dto.response.ApiResponse;
import com.codearena.dto.response.PageResponse;
import com.codearena.dto.response.SubmissionDetailResponse;
import com.codearena.dto.response.SubmissionResponse;
import com.codearena.entity.Language;
import com.codearena.entity.Verdict;
import com.codearena.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submit Code (graded + persisted) and view your own submission history")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Submit code for grading against a problem's full test-case set")
    public ResponseEntity<ApiResponse<SubmissionDetailResponse>> submitCode(
            @Valid @RequestBody SubmitCodeRequest request, Authentication authentication) {
        SubmissionDetailResponse response = submissionService.submitCode(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Submission graded", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of your own submissions in full detail (source code included)")
    public ResponseEntity<ApiResponse<SubmissionDetailResponse>> getOwnSubmission(
            @PathVariable Long id, Authentication authentication) {
        SubmissionDetailResponse response = submissionService.getOwnSubmission(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Paginated submission history for the current user, optionally filtered")
    public ResponseEntity<ApiResponse<PageResponse<SubmissionResponse>>> getMySubmissions(
            @RequestParam(required = false) Long problemId,
            @RequestParam(required = false) Verdict verdict,
            @RequestParam(required = false) Language language,
            @PageableDefault(size = 20, sort = "id") Pageable pageable,
            Authentication authentication) {
        PageResponse<SubmissionResponse> response = submissionService.getMySubmissions(
                authentication.getName(), problemId, verdict, language, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
