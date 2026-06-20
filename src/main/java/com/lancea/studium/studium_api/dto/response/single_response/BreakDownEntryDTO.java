package com.lancea.studium.studium_api.dto.response.single_response;

import java.time.LocalDate;

public record BreakDownEntryDTO(LocalDate periodStart,
                                Long sessions,
                                Long focusMinutes,
                                Double completionRate
                                ){
}
