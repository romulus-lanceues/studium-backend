package com.lancea.studium.studium_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Email(message = "Enter a valid email")
        @NotBlank(message = "Email must not blank")
        String email,

        @Size(min = 8, message = "Password must not be shorter than 8 characters", groups = ValidationGroups.Update.class)
        @NotBlank(message = "Password must not be blank", groups = ValidationGroups.Update.class)
        String password
) {
}
