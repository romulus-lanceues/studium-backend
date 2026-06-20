package com.lancea.studium.studium_api.dto.response.bundled_response;

import com.lancea.studium.studium_api.dto.response.single_response.SubjectGoalDTO;

import java.util.List;

public record GoalProgressDTO(Integer totalWeeklyGoal,
                              Long totalCompletedThisWeek,
                              Double weeklyProgress,
                              List<SubjectGoalDTO> subjects) {
}
