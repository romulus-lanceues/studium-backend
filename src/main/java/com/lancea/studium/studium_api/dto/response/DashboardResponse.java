package com.lancea.studium.studium_api.dto.response;

import com.lancea.studium.studium_api.entity.StudySession;

import java.util.List;

//DTO used to retrieve the details the dashboard request will need
public record DashboardResponse(Integer streak, String lastSession,
                                Integer sessionsCompletedToday,
                                List<StudySession> recentSessions) {
}
