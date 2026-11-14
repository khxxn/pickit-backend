package com.pickit.backend.popup.dto.response;

import com.pickit.backend.popup.entity.PopupStatus;
import java.time.LocalDate;

public record PopupDetailResponse(
        Long popupId,
        String name,
        String thumbnailUrl,
        Long categoryId,
        String categoryName,
        String district,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        PopupStatus status,
        String description
) {
}
