package com.lancea.studium.studium_api.dto.request;

import com.lancea.studium.studium_api.entity.SessionType;

public record StartSessionRequest(Long subjectId,
                                  SessionType sessionType,
                                  Integer plannedDuration,
                                  String notes){}
