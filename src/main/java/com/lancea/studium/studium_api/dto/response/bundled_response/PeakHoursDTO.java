package com.lancea.studium.studium_api.dto.response.bundled_response;

import com.lancea.studium.studium_api.dto.response.single_response.HourlyStatDTO;

import java.util.List;

public record PeakHoursDTO(String peakHour,
                           List<HourlyStatDTO> distribution) {
}
