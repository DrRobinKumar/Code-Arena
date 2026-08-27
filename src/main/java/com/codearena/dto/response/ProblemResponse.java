package com.codearena.dto.response;

import com.codearena.entity.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The user-facing problem detail. Deliberately has NO editorial field and
 * its testCases list is pre-filtered (in ProblemServiceImpl) to only
 * hidden=false entries — regular users must never receive the grading-only
 * hidden test cases or the solution editorial through this endpoint.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    private Long id;
    private String title;
    private String slug;
    private DifficultyLevel difficulty;
    private String description;
    private String constraints;
    private String inputFormat;
    private String outputFormat;
    private Integer timeLimitMs;
    private Integer memoryLimitKb;
    private List<String> hints;
    private List<ExampleResponse> examples;
    private List<String> tags;

    /** Visible (hidden=false) test cases only — safe sample cases for a future "Run" feature. */
    private List<TestCaseResponse> visibleTestCases;
}
