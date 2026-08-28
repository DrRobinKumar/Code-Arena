package com.codearena.dto.request;

import com.codearena.entity.DifficultyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Deliberately a full-replace contract for Phase 2: every collection
 * (tags/examples/hints/testCases) sent here replaces what's currently
 * stored, matching Problem.replaceTestCases/replaceProblemTags. A partial
 * PATCH-style update can be layered in later without breaking this one.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemUpdateRequest {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    /** Optional — if blank, kept as-is unless the title changed, in which case it's regenerated. */
    @Size(max = 220, message = "slug must be at most 220 characters")
    private String slug;

    @NotNull(message = "difficulty is required")
    private DifficultyLevel difficulty;

    @NotBlank(message = "description is required")
    private String description;

    private String constraints;

    private String inputFormat;

    private String outputFormat;

    private String editorial;

    @NotNull(message = "timeLimitMs is required")
    @Min(value = 100, message = "timeLimitMs must be at least 100")
    private Integer timeLimitMs;

    @NotNull(message = "memoryLimitKb is required")
    @Min(value = 1024, message = "memoryLimitKb must be at least 1024")
    private Integer memoryLimitKb;

    private List<@NotBlank(message = "hint must not be blank") String> hints;

    @Valid
    private List<ExampleRequest> examples;

    private List<@NotBlank(message = "tag name must not be blank") String> tags;

    @NotEmpty(message = "at least one test case is required")
    @Valid
    private List<TestCaseRequest> testCases;
}
