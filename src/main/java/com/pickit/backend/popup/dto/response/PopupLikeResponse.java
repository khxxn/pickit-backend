package com.pickit.backend.popup.dto.response;

public record PopupLikeResponse(
        Long popupId,
        boolean isLiked,
        long likeCount
) {
}
