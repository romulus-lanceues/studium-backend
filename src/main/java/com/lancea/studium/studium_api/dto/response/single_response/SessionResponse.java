package com.lancea.studium.studium_api.dto.response.single_response;

import com.lancea.studium.studium_api.entity.SessionStatus;
import com.lancea.studium.studium_api.entity.StudySession;

import java.time.LocalDateTime;

public record SessionResponse(Long id,
                              String subjectName,
                              Integer plannedDurationMinutes,
                              Integer actualDurationMinutes,
                              SessionStatus sessionStatus,
                              LocalDateTime startTime,
                              LocalDateTime endTime) {


    public static SessionResponse from(StudySession studySession){
        return new SessionResponse(
                studySession.getId(),
                studySession.getSubject().getName(),
                studySession.getPlannedDurationMinutes(),
                studySession.getActualDurationMinutes(),
                studySession.getSessionStatus(),
                studySession.getStartTime(),
                studySession.getEndTime()
        );
    }
}
