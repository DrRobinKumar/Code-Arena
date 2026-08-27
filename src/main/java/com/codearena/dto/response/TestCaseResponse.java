package com.codearena.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private boolean hidden;
}
