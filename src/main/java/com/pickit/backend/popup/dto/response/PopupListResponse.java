package com.pickit.backend.popup.dto.response;

import java.util.List;

public record PopupListResponse(
        List<PopupListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
