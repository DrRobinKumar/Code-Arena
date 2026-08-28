package com.codearena.dto.request;

import com.codearena.entity.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCodeRequest {

    @NotBlank(message = "problemSlug is required")
    private String problemSlug;

    @NotNull(message = "language is required")
    private Language language;

    @NotBlank(message = "sourceCode is required")
    @Size(max = 20000, message = "sourceCode must be at most 20000 characters")
    private String sourceCode;
}
