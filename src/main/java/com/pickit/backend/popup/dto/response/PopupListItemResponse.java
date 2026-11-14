package com.pickit.backend.popup.dto.response;

import com.pickit.backend.popup.entity.PopupStatus;
import java.time.LocalDate;

public record PopupListItemResponse(
        Long popupId,
        String name,
        String thumbnailUrl,
        String district,
        LocalDate startDate,
        LocalDate endDate,
        PopupStatus status,
        boolean isLiked
) {
}
