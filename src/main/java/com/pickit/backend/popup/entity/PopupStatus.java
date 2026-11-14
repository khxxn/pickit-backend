package com.pickit.backend.popup.entity;

import java.time.LocalDate;

public enum PopupStatus {
    ONGOING,
    UPCOMING,
    ENDED;

    public static PopupStatus fromDates(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (today.isBefore(startDate)) {
            return UPCOMING;
        }
        if (today.isAfter(endDate)) {
            return ENDED;
        }
        return ONGOING;
    }
}
