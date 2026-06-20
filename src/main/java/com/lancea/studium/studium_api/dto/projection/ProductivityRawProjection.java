package com.lancea.studium.studium_api.dto.projection;

public interface ProductivityRawProjection {
    Double getCompletionRate();
    Double getConsistencyRate();
    Long getTotalSessions();
    Double getAverageInterruptions();
}
