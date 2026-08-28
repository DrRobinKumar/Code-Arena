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
public class RunCodeRequest {

    @NotNull(message = "language is required")
    private Language language;

    @NotBlank(message = "sourceCode is required")
    @Size(max = 20000, message = "sourceCode must be at most 20000 characters")
    private String sourceCode;

    /** Custom input for this run. Blank/omitted means the program is run with empty stdin. */
    private String stdin;

    /** Optional — when set, the referenced problem's time/memory limits are used instead of the defaults. */
    private String problemSlug;
}
