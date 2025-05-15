package com.mshzidan.guard.security.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

public class PasswordUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String generateSecureRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    public static String generateHashedPassword() {
        String rawPassword = generateSecureRandomPassword();
        return encoder.encode(rawPassword);
    }

    // Password complexity validation (for example, at least 8 characters)
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        // Check password length
        if (password.length() < 8) {
            return false;
        }
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // Check for at least one special character
        return password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
}