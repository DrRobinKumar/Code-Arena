package com.codearena.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private int status;
    private String error;
    private String message;
    private String path;

    /** Field-level validation errors, e.g. "email: must be a well-formed email address". */
    private List<String> details;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
