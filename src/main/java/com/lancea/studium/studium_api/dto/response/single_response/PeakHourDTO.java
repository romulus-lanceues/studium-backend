package com.lancea.studium.studium_api.dto.response.single_response;

public record PeakHourDTO(Long hourOfTheDay,
                          Long completedSession,
                          Double avgActualMinutes) {
}
