package com.lancea.studium.studium_api.dto.response.single_response;

public record DurationBucketDTO(
        Integer plannedDurationMinutes,
        Long totalSessions,
        Long completedSessions,
        Double completionRate,
        Double avgInterruptions


) {
}
