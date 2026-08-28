package com.codearena.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 30, message = "username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username may only contain letters, digits and underscores")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "must be a well-formed email address")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 100, message = "password must be at least 8 characters")
    private String password;

    @NotBlank(message = "fullName is required")
    @Size(max = 100, message = "fullName must be at most 100 characters")
    private String fullName;
}
