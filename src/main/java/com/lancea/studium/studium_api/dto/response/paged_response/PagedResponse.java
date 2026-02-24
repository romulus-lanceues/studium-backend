package com.lancea.studium.studium_api.dto.response.paged_response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {

    public static <T> PagedResponse<T> from(Page<T> results){
        return new PagedResponse<T>(
                results.getContent(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages(),
                results.isFirst(),
                results.isLast(),
                results.isEmpty()
        );
    }
}
