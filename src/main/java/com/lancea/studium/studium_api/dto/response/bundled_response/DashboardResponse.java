package com.lancea.studium.studium_api.dto.response.bundled_response;

//DTO used to retrieve the details the dashboard request will need
public record DashboardResponse(String username, Integer streak, String lastSession,
                                Integer sessionsCompletedToday) {
}
