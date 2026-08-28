package com.codearena.dto.response;

import com.codearena.entity.Language;
import com.codearena.entity.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Deliberately excludes sourceCode/compileOutput/errorMessage — those are detail-view-only. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private String problemTitle;
    private String problemSlug;
    private String username;
    private Language language;
    private Verdict verdict;
    private Long runtimeMs;
    private Long memoryKb;
    private Integer testCasesPassed;
    private Integer testCasesTotal;
    private LocalDateTime createdAt;
}
