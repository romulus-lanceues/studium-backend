package com.lancea.studium.studium_api.dto.response.single_response;

public record SubjectGoalDTO(Long subjectId,
                             String subjectName,
                             Integer weeklyGoal,
                             Long completedThisWeek,
                             Double progress) {
}
