package com.pickit.backend.verification.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record VerificationResponse(
        List<PopupVerificationItemResponse> content,
        int totalElements,
        long verifiedCount,
        int verificationRadiusMeters,
        long updateIntervalSeconds,
        OffsetDateTime serverTime
) {
}
