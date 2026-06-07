package com.lancea.studium.studium_api.dto.request;

import com.lancea.studium.studium_api.entity.Subject;

public record UpdateSubjectRequest(String subjectName, String subjectDescription,
                                    Integer weeklyGoalSessions, String subjectColor ) {


}

