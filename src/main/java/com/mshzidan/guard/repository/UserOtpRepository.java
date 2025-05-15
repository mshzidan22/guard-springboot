package com.mshzidan.guard.repository;

import com.mshzidan.guard.security.entites.OtpType;
import com.mshzidan.guard.security.entites.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {

    @Query("SELECT o FROM UserOtp o WHERE o.recipient = :recipient AND o.otpType = :otpType " +
            "AND o.status = 'PENDING' AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<UserOtp> findLatestActiveOtp(@Param("recipient") String recipient,
                                          @Param("otpType") OtpType otpType,
                                          @Param("now") LocalDateTime now);

    // Find all expired but still pending OTPs
    @Query("SELECT o FROM UserOtp o WHERE o.status = 'PENDING' AND o.expiresAt < :now")
    Iterable<UserOtp> findExpiredPendingOtps(@Param("now") LocalDateTime now);
}
