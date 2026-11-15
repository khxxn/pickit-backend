package com.pickit.backend.verification.service;

import java.time.Duration;
import java.time.ZoneId;

public final class VerificationPolicy {

    public static final int RADIUS_METERS = 200;
    public static final int MAX_ACCURACY_METERS = 100;
    public static final Duration MAX_LOCATION_AGE = Duration.ofMinutes(1);
    public static final Duration VALIDITY = Duration.ofMinutes(10);
    public static final Duration UPDATE_INTERVAL = Duration.ofMinutes(5);
    public static final ZoneId DATABASE_ZONE = ZoneId.of("Asia/Seoul");

    private VerificationPolicy() {
    }
}
