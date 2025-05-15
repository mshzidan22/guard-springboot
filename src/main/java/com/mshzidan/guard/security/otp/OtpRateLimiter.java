package com.mshzidan.guard.security.otp;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpRateLimiter {

    private final Map<String, LocalDateTime> otpRequestTimestamps = new ConcurrentHashMap<>();

    public boolean isAllowed(String key) {
        LocalDateTime lastRequest = otpRequestTimestamps.get(key);
        LocalDateTime now = LocalDateTime.now();

        if (lastRequest != null && Duration.between(lastRequest, now).getSeconds() < 60) {
            return false; // 1 minute cooldown
        }
        otpRequestTimestamps.put(key, now);
        return true;
    }
}
