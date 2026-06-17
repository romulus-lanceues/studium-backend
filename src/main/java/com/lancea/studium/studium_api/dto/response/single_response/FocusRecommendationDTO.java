package com.lancea.studium.studium_api.dto.response.single_response;

public record FocusRecommendationDTO(Integer recommendedFocusMinutes,
                                     Double confidence,
                                     Long basedOnSessions,
                                     String peakProductivityHour,
                                     String insight,
                                     Boolean sufficientData) {
}

