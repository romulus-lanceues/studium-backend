package com.lancea.studium.studium_api.dto.projection;

/**
 * An Interface-based projection for the fetchSummaryStats query call due to its complex nature
 */
public interface SummaryStatsProjection {
    Long getTotalSessions();
    Long getCompletedSessions();
    Double getCompletionRate();
    Long getTotalFocusMinutes();
    Double getAverageInterruptions();
}
