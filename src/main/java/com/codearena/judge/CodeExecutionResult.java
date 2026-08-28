package com.codearena.judge;

import com.codearena.entity.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResult {
    private Verdict verdict;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private Long executionTimeMs;
    private Long memoryKb;
}
