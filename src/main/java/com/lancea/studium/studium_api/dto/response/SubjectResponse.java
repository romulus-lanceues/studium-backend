package com.lancea.studium.studium_api.dto.response;

public record SubjectResponse(Long  subjectId,
                              String subjectName,
                              String color,
                              String subjectDescription,
                              Integer weeklyGoalMinutes,
                              Integer totalStudyTime){}
