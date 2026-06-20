package com.lancea.studium.studium_api.dto.projection;

public interface SubjectGoalProjection {
    Long getSubjectId();
    String getSubjectName();
    Integer getWeeklyGoal();
    Long getCompletedThisWeek();
}
