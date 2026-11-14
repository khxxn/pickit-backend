package com.pickit.backend.popup.entity;

import java.time.LocalDate;

public enum PopupStatus {
    ONGOING("진행 중"),
    UPCOMING("진행 전"),
    ENDED("진행 완료");

    private final String label;

    PopupStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

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
