package com.codearena.controller;

import com.codearena.dto.request.ProblemCreateRequest;
import com.codearena.dto.request.ProblemUpdateRequest;
import com.codearena.dto.response.ApiResponse;
import com.codearena.dto.response.ProblemAdminResponse;
import com.codearena.service.ProblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/problems")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Problems", description = "Problem CRUD, admin only")
public class AdminProblemController {

    private final ProblemService problemService;

    @PostMapping
    @Operation(summary = "Create a new problem, with its tags and test cases")
    public ResponseEntity<ApiResponse<ProblemAdminResponse>> createProblem(
            @Valid @RequestBody ProblemCreateRequest request) {
        ProblemAdminResponse response = problemService.createProblem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Problem created", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a problem (full replace of tags/examples/hints/testCases)")
    public ResponseEntity<ApiResponse<ProblemAdminResponse>> updateProblem(
            @PathVariable Long id, @Valid @RequestBody ProblemUpdateRequest request) {
        ProblemAdminResponse response = problemService.updateProblem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Problem updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a problem (cascades to its test cases and tag links)")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.ok(ApiResponse.success("Problem deleted", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full problem detail for editing (includes editorial and hidden test cases)")
    public ResponseEntity<ApiResponse<ProblemAdminResponse>> getForAdmin(@PathVariable Long id) {
        ProblemAdminResponse response = problemService.getProblemForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
