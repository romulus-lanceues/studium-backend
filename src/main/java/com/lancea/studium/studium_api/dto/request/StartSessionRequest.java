package com.lancea.studium.studium_api.dto.request;

import com.lancea.studium.studium_api.shared.enums.SessionType;

public record StartSessionRequest(SessionType sessionType,
                                  Integer plannedDuration,
                                  String notes){}
