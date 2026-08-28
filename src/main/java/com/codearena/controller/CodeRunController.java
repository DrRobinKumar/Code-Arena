package com.codearena.controller;

import com.codearena.dto.request.RunCodeRequest;
import com.codearena.dto.response.ApiResponse;
import com.codearena.dto.response.RunCodeResponse;
import com.codearena.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/run")
@RequiredArgsConstructor
@Tag(name = "Run Code", description = "Ad-hoc code execution against custom input — never persisted")
public class CodeRunController {

    private final SubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Run code against custom (or problem-derived) input and get raw output back")
    public ResponseEntity<ApiResponse<RunCodeResponse>> runCode(@Valid @RequestBody RunCodeRequest request) {
        RunCodeResponse response = submissionService.runCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
