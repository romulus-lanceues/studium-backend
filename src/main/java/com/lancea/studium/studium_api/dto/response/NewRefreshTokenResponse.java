package com.lancea.studium.studium_api.dto.response;

public record NewRefreshTokenResponse(
        String accessToken,
        String refreshToken ) {}
