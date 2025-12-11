package com.lancea.studium.studium_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//Will be use in update password feature
public record RegisterRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email shouldn't be blanked")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must not be shorter than 8 characters")
        String password,

        @NotBlank(message = "Name must not be blank")
        @Size(min = 3, message = "Name must be at least 3 characters long")
        String fullName) {}
