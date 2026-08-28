package com.codearena.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExampleRequest {

    @NotBlank(message = "example input is required")
    private String input;

    @NotBlank(message = "example output is required")
    private String output;

    private String explanation;
}
