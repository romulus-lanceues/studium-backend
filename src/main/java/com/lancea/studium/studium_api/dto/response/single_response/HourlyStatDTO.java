package com.lancea.studium.studium_api.dto.response.single_response;

public record HourlyStatDTO(String hour,
                            Long sessions,
                            Double completionRate) {
}
