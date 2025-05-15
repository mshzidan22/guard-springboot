package com.mshzidan.guard.security.otp;

public interface OtpSender {
    void sendOtp(String recipient, String code);
    default boolean verifyOTP(String recipient, String code){ return true; }
}
