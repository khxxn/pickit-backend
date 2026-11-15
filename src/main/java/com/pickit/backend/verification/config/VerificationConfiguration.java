package com.pickit.backend.verification.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerificationConfiguration {

    @Bean
    public Clock verificationClock() {
        return Clock.systemUTC();
    }
}
