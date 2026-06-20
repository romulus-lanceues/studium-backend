package com.lancea.studium.studium_api.dto.response.bundled_response;

import com.lancea.studium.studium_api.dto.response.single_response.BreakDownEntryDTO;
import com.lancea.studium.studium_api.shared.enums.BreakDownPeriod;

import java.util.List;

public record BreakDownDTO(BreakDownPeriod breakDownPeriod,
                           List<BreakDownEntryDTO> data) {
}
