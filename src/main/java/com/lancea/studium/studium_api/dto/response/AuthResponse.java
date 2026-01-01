package com.lancea.studium.studium_api.dto.response;

public record AuthResponse(
        Long id,
        String email,
        String fullName,
        String token,
        String refreshToken
) {}
