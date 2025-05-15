package com.mshzidan.guard.security.otp;

import com.mshzidan.guard.security.utils.Util;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("twilioSender")
public class TwilioOtpSender implements OtpSender{
    private final String accountSid;
    private final String authToken;
    private final String verifyServiceSid;

    public TwilioOtpSender(@Value("${twilio.account.sid}") String accountSid, @Value("${twilio.auth.token}") String authToken, @Value("${twilio.verify.service.sid}") String verifyServiceSid) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.verifyServiceSid = verifyServiceSid;
        Twilio.init(accountSid, authToken);
    }

    @Override
    public void sendOtp(String number, String code) {
        if (!Util.isValidPhoneNumber(number)) {
            throw new IllegalArgumentException("Invalid phone number");
        }
        Verification verification = Verification.creator(verifyServiceSid, Util.formatPhoneNumber(number), "sms").setCustomCode(code).create();
        System.out.println(verification.getStatus());
    }

    @Override
    public boolean verifyOTP(String phoneNumber, String code) {
        VerificationCheck verificationCheck = VerificationCheck.creator(verifyServiceSid).setTo(Util.formatPhoneNumber(phoneNumber)).setCode(code).create();
        return verificationCheck.getStatus().equals("approved"); // "approved" means success
    }
}
