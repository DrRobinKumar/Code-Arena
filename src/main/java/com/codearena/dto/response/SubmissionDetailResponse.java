package com.codearena.dto.response;

import com.codearena.entity.Language;
import com.codearena.entity.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Includes the submitted source code and any compile/error diagnostics —
 * access to this is restricted (owner or admin only) in
 * SubmissionServiceImpl, since source code and stderr can be sensitive.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDetailResponse {
    private Long id;
    private String problemTitle;
    private String problemSlug;
    private String username;
    private Language language;
    private String sourceCode;
    private Verdict verdict;
    private Long runtimeMs;
    private Long memoryKb;
    private Integer testCasesPassed;
    private Integer testCasesTotal;
    private String compileOutput;
    private String errorMessage;
    private LocalDateTime createdAt;
}
