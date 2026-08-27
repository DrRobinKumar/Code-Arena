package com.codearena.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagCreateRequest {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must be at most 50 characters")
    private String name;
}
