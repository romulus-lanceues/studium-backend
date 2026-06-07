package com.lancea.studium.studium_api.dto.response.single_response;

import java.time.LocalDateTime;

public record ExceptionResponse(
        LocalDateTime date,
        String message,
        int status,
        String path
) {}