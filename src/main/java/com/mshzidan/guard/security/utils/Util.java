package com.mshzidan.guard.security.utils;

import com.mshzidan.guard.security.entites.OtpType;
import com.mshzidan.guard.security.entites.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {


    public static OtpType getRecipientType(String recipient) {
        return (recipient.contains("@")) ? OtpType.EMAIL : OtpType.PHONE;
    }

    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * @return if the phone number is valid for Egypt or Saudi Arabia.
     */

    public static Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        // Handle null input set gracefully
        if (roles == null) {
            return new java.util.ArrayList<>(); // Return an empty list
        }
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList()); // Collect the results into a List
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Check for Egypt (EG) numbers
        // Formats:
        // 01XXXXXXXXX (10 digits)
        // +201XXXXXXXXX or 201XXXXXXXXX (12 digits)
        boolean isEgyptValid = cleanedNumber.matches("^(01[0-9]{9})$") ||  // Local format (10 digits)
                cleanedNumber.matches("^(201[0-9]{9})$") ||  // Without + (12 digits)
                cleanedNumber.matches("^(\\+201[0-9]{9})$"); // With + (12 digits)

        // Check for Saudi Arabia (KSA) numbers
        // Formats:
        // 05XXXXXXXX (9 digits)
        // +9665XXXXXXXX or 9665XXXXXXXX (12 digits)
        boolean isSaudiValid = cleanedNumber.matches("^(05[0-9]{8})$") ||   // Local format (9 digits)
                cleanedNumber.matches("^(9665[0-9]{8})$") || // Without + (12 digits)
                cleanedNumber.matches("^(\\+9665[0-9]{8})$"); // With + (12 digits)

        return isEgyptValid || isSaudiValid;
    }

    public static String formatPhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("01")) {
            return "+20" + cleaned; // Egypt
        } else if (cleaned.startsWith("05")) {
            return "+966" + cleaned.substring(1); // Saudi
        }
        return phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
    }

    public static String normalizePhoneNumber(String phone) {
        if (phone == null) return null;

        // Remove non-digit characters
        phone = phone.replaceAll("[^\\d+]", "");

        // Handle KSA numbers
        if (phone.startsWith("00966")) {
            phone = "+" + phone.substring(4);
        } else if (phone.startsWith("966")) {
            phone = "+966" + phone.substring(3);
        } else if (phone.startsWith("05")) {
            phone = "+966" + phone.substring(1); // Convert 05XXXXXXXX to +9665XXXXXXXX
        }

        // Handle Egypt numbers
        else if (phone.startsWith("0020")) {
            phone = "+" + phone.substring(3);
        } else if (phone.startsWith("20")) {
            phone = "+20" + phone.substring(2);
        } else if (phone.startsWith("01")) {
            phone = "+20" + phone.substring(1); // Convert 01XXXXXXXX to +201XXXXXXXXX
        }

        // Final check
        if (isValidE164(phone)) {
            return phone;
        }
        return null;
    }

    public static boolean isValidE164(String phone) {
        return phone != null && phone.matches("^\\+(9665\\d{8}|201\\d{8})$");
    }

}
