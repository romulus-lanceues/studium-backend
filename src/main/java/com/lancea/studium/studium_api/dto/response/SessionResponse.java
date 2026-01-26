package com.lancea.studium.studium_api.dto.response;

import com.lancea.studium.studium_api.entity.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(Long id,
                              String subjectName,
                              Integer plannedDurationMinutes,
                              Integer actualDurationMinutes,
                              SessionStatus sessionStatus,
                              LocalDateTime startTime,
                              LocalDateTime endTime) {
}
