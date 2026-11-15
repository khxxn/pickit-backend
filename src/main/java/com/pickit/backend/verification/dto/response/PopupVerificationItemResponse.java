package com.pickit.backend.verification.dto.response;

import com.pickit.backend.popup.entity.PopupStatus;
import com.pickit.backend.verification.entity.PresenceStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PopupVerificationItemResponse(
        Long verificationId,
        Long popupId,
        String name,
        String thumbnailUrl,
        String district,
        String address,
        LocalDate startDate,
        LocalDate endDate,
        PopupStatus popupStatus,
        double distanceMeters,
        boolean isVerified,
        PresenceStatus presenceStatus,
        OffsetDateTime verifiedAt,
        OffsetDateTime expiredAt,
        OffsetDateTime lastLocationAt,
        OffsetDateTime leftAt
) {
}
