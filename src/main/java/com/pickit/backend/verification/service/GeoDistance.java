package com.pickit.backend.verification.service;

import java.math.BigDecimal;

public final class GeoDistance {

    // 기존 MySQL ST_Distance_Sphere의 SRID 0 기본 구면 반지름과 동일하게 계산한다.
    private static final double EARTH_RADIUS_METERS = 6_370_986;

    private GeoDistance() {
    }

    public static double meters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        double latitude1 = Math.toRadians(lat1.doubleValue());
        double latitude2 = Math.toRadians(lat2.doubleValue());
        double halfLat = Math.sin((latitude2 - latitude1) / 2);
        double halfLng = Math.sin(Math.toRadians(lng2.doubleValue() - lng1.doubleValue()) / 2);
        double a = halfLat * halfLat + Math.cos(latitude1) * Math.cos(latitude2) * halfLng * halfLng;
        a = Math.max(0, Math.min(1, a));
        return 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
