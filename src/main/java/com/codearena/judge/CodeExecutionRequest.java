package com.codearena.judge;

import com.codearena.entity.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Deliberately judge-agnostic: no Judge0-specific field names (no
 * "cpu_time_limit", no base64 flags). Judge0ExecutionServiceImpl is the
 * only class that knows how to translate this into Judge0's wire format;
 * a future Docker-sandbox implementation would translate it into
 * container run arguments instead. Nothing above CodeExecutionService
 * should ever import a Judge0-specific type.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionRequest {
    private Language language;
    private String sourceCode;
    private String stdin;

    /** Null for a plain Run; set for Submit, where Judge0 does the output comparison itself. */
    private String expectedOutput;

    private int timeLimitMs;
    private int memoryLimitKb;
}
