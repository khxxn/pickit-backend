package com.pickit.backend.popup.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface NearbyPopupProjection {

    Long getPopupId();

    String getName();

    String getThumbnailUrl();

    String getDistrict();

    String getAddress();

    BigDecimal getLatitude();

    BigDecimal getLongitude();

    LocalDate getStartDate();

    LocalDate getEndDate();

    Double getDistanceMeters();
}
