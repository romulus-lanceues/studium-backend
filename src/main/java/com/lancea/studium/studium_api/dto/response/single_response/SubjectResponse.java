package com.lancea.studium.studium_api.dto.response.single_response;

import com.lancea.studium.studium_api.entity.Subject;

import java.time.LocalDate;


public record SubjectResponse(Long  subjectId,
                              String subjectName,
                              String color,
                              String subjectDescription,
                              Integer weeklyGoalSessions,
                              Integer totalStudyTime,
                              Integer pomodorosCompleted,
                              Integer streak,
                              LocalDate lastSession){


    public static SubjectResponse from(Subject subject){
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getColor(),
                subject.getDescription(),
                subject.getWeeklyGoalSessions(),
                subject.getTotalStudyTime(),
                subject.getPomodorosCompleted(),
                subject.getStreak(),
                subject.getLastSession());
    }

}
