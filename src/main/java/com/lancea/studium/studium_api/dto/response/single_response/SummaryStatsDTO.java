package com.lancea.studium.studium_api.dto.response.single_response;

public record SummaryStatsDTO( Long totalSessions,
                               Long completedSessions,
                               Double completionRate,
                               Long totalFocusMinutes,
                               Integer currentStreak,
                               Integer longestStreak,
                               Double focusQuality
) {
}
