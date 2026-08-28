package com.codearena.dto.response;

import com.codearena.entity.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeResponse {
    private Verdict status;
    private String stdout;
    private String stderr;
    private String compileOutput;
    private Long executionTimeMs;
    private Long memoryKb;
}
