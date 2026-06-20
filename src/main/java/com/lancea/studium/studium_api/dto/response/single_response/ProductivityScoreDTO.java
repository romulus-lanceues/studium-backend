package com.lancea.studium.studium_api.dto.response.single_response;

import com.lancea.studium.studium_api.shared.enums.ProductivityTrend;

public record ProductivityScoreDTO(Integer score,
                                   Double completionWeight,
                                   Double consistencyWeight,
                                   Double volumeWeight,
                                   Double focusQualityWeight,
                                   ProductivityTrend productivityTrend
) {}
