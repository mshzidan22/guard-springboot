package com.mshzidan.guard.security.otp;

import com.mshzidan.guard.repository.UserOtpRepository;
import com.mshzidan.guard.security.entites.OtpStatus;
import com.mshzidan.guard.security.entites.UserOtp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OtpCleanupTask {

    private final UserOtpRepository otpRepository;

    public OtpCleanupTask(UserOtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Scheduled(fixedRateString = "${otp.cleanup.interval:60000}")
    public void markExpiredOTPs() {
       log.info("Cleaning up expired OTPs...");
        Iterable<UserOtp> expired = otpRepository.findExpiredPendingOtps(LocalDateTime.now());
        expired.forEach(otp -> {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
        });
    }
}
