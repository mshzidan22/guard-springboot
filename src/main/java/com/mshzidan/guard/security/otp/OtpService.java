package com.mshzidan.guard.security.otp;

import com.mshzidan.guard.repository.UserOtpRepository;
import com.mshzidan.guard.security.entites.OtpStatus;
import com.mshzidan.guard.security.entites.OtpType;
import com.mshzidan.guard.security.entites.UserOtp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class OtpService {

    private final Map<String, OtpSender> senderMap;
    private final String emailSenderBean;
    private final String phoneSenderBean;
    private final OtpGenerator otpGenerator;
    private final UserOtpRepository otpRepository;
    private final int otpExpirationMinutes;
    private final int maxAttempts;
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);




    public OtpService(Map<String, OtpSender> senderMap,
                      @Value("${otp.sender.email}") String emailSenderBean,
                      @Value("${otp.sender.phone}") String phoneSenderBean,
                      OtpGenerator otpGenerator,
                      UserOtpRepository otpRepository,
                      @Value("${otp.expiration.minutes:5}") int otpExpirationMinutes,
                      @Value("${otp.max.attempts:3}") int maxAttempts) {
        this.senderMap = senderMap;
        this.emailSenderBean = emailSenderBean;
        this.phoneSenderBean = phoneSenderBean;
        this.otpGenerator = otpGenerator;
        this.otpRepository = otpRepository;
        this.otpExpirationMinutes = otpExpirationMinutes;
        this.maxAttempts = maxAttempts;
    }
    @Transactional
    public void generateAndSendOtp(String recipient, OtpType otpType, String ipAddress) {
        invalidateExistingOTPs(recipient, otpType);

        String otpCode = otpGenerator.generateCode();

        UserOtp otpEntity = saveOtpRecord(recipient, otpCode, otpType, ipAddress);

        // Pick sender bean name based on OTP type
        String senderBean = (otpType == OtpType.EMAIL) ? emailSenderBean : phoneSenderBean;
        OtpSender sender = senderMap.get(senderBean);

        if (sender == null) {
            throw new IllegalArgumentException("No OtpSender found for bean: " + senderBean);
        }

        sender.sendOtp(recipient, otpCode);
        log.info("OTP {} sent to {} via {}", otpCode, recipient, senderBean);
    }

    private void invalidateExistingOTPs(String recipient, OtpType otpType) {
        Optional<UserOtp> existingOtp = otpRepository.findLatestActiveOtp(
                recipient, otpType, LocalDateTime.now());

        existingOtp.ifPresent(otp -> {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
        });
    }

    private UserOtp saveOtpRecord(String recipient, String otpCode, OtpType otpType, String ipAddress) {
        UserOtp otpEntity = UserOtp.builder()
                .recipient(recipient)
                .otpCode(otpCode)
                .otpType(otpType)
                .status(OtpStatus.PENDING)
                .verificationAttempts(0)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .ipAddress(ipAddress)
                .build();

        // Save to database
        return otpRepository.save(otpEntity);
    }

    public boolean verifyOtp(String recipient, String otpCode, OtpType otpType, String ipAddress) {
        // Find the latest active OTP for this recipient
        Optional<UserOtp> otpOptional = otpRepository.findLatestActiveOtp(recipient, otpType, LocalDateTime.now());

        if (otpOptional.isEmpty()) {
            return false; // No active OTP found
        }

        UserOtp otp = otpOptional.get();

        // Check if OTP is expired
        if (otp.isExpired()) {
            otp.setStatus(OtpStatus.EXPIRED);
            otpRepository.save(otp);
            return false;
        }

        // Check if code matches
        if (!otp.getOtpCode().equals(otpCode)) {
            // Increment attempts
            otp.incrementAttempts();

            // Check if max attempts reached
            if (otp.getVerificationAttempts() >= maxAttempts) {
                otp.setStatus(OtpStatus.FAILED);
            }

            otpRepository.save(otp);
            return false;
        }
        //Validate over Twilio needed for phone
        if (!senderMap.get(getSenderBean(otpType)).verifyOTP(recipient, otpCode)) {
            return false;
        }

        // OTP is valid - mark as verified
        otp.setStatus(OtpStatus.VERIFIED);
        otp.setVerifiedAt(LocalDateTime.now());
        otp.setIpAddress(ipAddress); // Update the IP used for verification
        otpRepository.save(otp);

        return true;
    }

    private String getSenderBean(OtpType otpType) {
        return otpType == OtpType.EMAIL ? emailSenderBean : phoneSenderBean;
    }


}
