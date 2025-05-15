package com.mshzidan.guard.security.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipient; // Could be email or phone number

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpType otpType; // EMAIL or PHONE

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpStatus status; // PENDING, VERIFIED, EXPIRED, FAILED

    @Column(nullable = false)
    private Integer verificationAttempts;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private String ipAddress;

    // Increment verification attempts
    public void incrementAttempts() {
        this.verificationAttempts++;
    }

    // Check if OTP is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
