package com.pickit.backend.popup.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record NearbyPopupResponse(
        BigDecimal latitude,
        BigDecimal longitude,
        int radiusMeters,
        List<NearbyPopupItemResponse> content,
        int totalElements
) {
}
