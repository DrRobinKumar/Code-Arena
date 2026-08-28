package com.codearena.dto.response;

import com.codearena.entity.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deliberately excludes description/constraints/examples/testCases —
 * list/search results should be cheap to render as a table row; the full
 * body is only fetched via GET /problems/{slug}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSummaryResponse {
    private Long id;
    private String title;
    private String slug;
    private DifficultyLevel difficulty;
    private List<String> tags;
    private LocalDateTime createdAt;
}
