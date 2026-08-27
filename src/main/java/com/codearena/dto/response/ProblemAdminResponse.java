package com.codearena.dto.response;

import com.codearena.entity.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Only ever returned from ADMIN-guarded endpoints (see
 * AdminProblemController) — includes the editorial and every test case
 * (hidden and visible), which must never leak to a regular user.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemAdminResponse {
    private Long id;
    private String title;
    private String slug;
    private DifficultyLevel difficulty;
    private String description;
    private String constraints;
    private String inputFormat;
    private String outputFormat;
    private String editorial;
    private Integer timeLimitMs;
    private Integer memoryLimitKb;
    private List<String> hints;
    private List<ExampleResponse> examples;
    private List<String> tags;
    private List<TestCaseResponse> testCases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
