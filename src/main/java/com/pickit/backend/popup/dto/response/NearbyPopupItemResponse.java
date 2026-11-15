package com.pickit.backend.popup.dto.response;

import com.pickit.backend.popup.entity.PopupStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record NearbyPopupItemResponse(
        Long popupId,
        String name,
        String thumbnailUrl,
        String district,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDate startDate,
        LocalDate endDate,
        PopupStatus status,
        double distanceMeters
) {
}
