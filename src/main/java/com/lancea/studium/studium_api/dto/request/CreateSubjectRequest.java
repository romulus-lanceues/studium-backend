package com.lancea.studium.studium_api.dto.request;

public record CreateSubjectRequest(
         String subjectName,
         String color,
         String description,
         Integer weeklyGoal) {}
