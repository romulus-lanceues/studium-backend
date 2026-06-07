package com.lancea.studium.studium_api.dto.response.single_response;

import java.time.LocalDate;

public record CompletedSessionSummary(
        LocalDate day,
        Long sessionCount
) {}
