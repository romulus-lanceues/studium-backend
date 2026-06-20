package com.lancea.studium.studium_api.dto.projection;

public interface PeakHourProjection {
    Integer getHour();
    Long getSessions();
    Double getCompletionRate();
}
