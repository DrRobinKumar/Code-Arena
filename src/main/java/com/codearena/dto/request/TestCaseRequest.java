package com.codearena.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseRequest {

    @NotBlank(message = "test case input is required")
    private String input;

    @NotBlank(message = "test case expectedOutput is required")
    private String expectedOutput;

    @NotNull(message = "hidden flag is required (true = graded only, false = visible/sample)")
    private Boolean hidden;
}
