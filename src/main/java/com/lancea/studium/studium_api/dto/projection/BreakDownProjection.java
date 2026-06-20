package com.lancea.studium.studium_api.dto.projection;

import java.time.LocalDateTime;

public interface BreakDownProjection {
    LocalDateTime getPeriodStart();
    Long getSessions();
    Long getFocusMinutes();
    Double getCompletionRate();
}
