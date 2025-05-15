package com.mshzidan.guard.security.otp;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpGenerator {

    public String generateCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit number
        return String.valueOf(otp);
    }
}
