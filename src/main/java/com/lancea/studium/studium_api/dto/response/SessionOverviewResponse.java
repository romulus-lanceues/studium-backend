package com.lancea.studium.studium_api.dto.response;

import com.lancea.studium.studium_api.entity.StudySession;

import java.util.List;

public record SessionOverviewResponse(Integer completedSessionsCount,
                                      Integer cancelledSessionsCount,
                                      List<SessionResponse> completedSessions,
                                      List<SessionResponse> cancelledSessions) {
}
