package com.mshzidan.guard.controller;

import com.mshzidan.guard.security.entites.OtpType;
import com.mshzidan.guard.security.otp.OtpRateLimiter;
import com.mshzidan.guard.security.otp.OtpService;
import com.mshzidan.guard.security.utils.Util;
import com.mshzidan.guard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/login")
@Slf4j
public class LoginController {

    private final OtpService otpService;
    private final UserService userService;
    private final OtpRateLimiter otpRateLimiter;

    @GetMapping
    public String login() {
        return "login";
    }

    @GetMapping("/email")
    public String loginWithEmail() {
        return "email-entry";
    }

    @GetMapping("/phone")
    public String loginWithPhone() {
        return "phone-entry";
    }

    @PostMapping("/email/send-otp")
    public String sendEmailOtp(@RequestParam String email, HttpSession session, HttpServletRequest request, Model model) {
        return sendOtp(email, "email", session, request, model, true);
    }

    @PostMapping("/phone/send-otp")
    public String sendPhoneOtp(@RequestParam String phone, HttpSession session, HttpServletRequest request, Model model) {
        return sendOtp(phone, "phone", session, request, model, false);
    }

    //TODO Move to service class
    private String sendOtp(String identifier, String type, HttpSession session, HttpServletRequest request, Model model, boolean isEmail) {
        String clientIp = Util.getClientIp(request);
        String rateLimitKey = clientIp + ":" + identifier;

        // 1. Validate input
        if (isEmail) {
            identifier = identifier.trim().toLowerCase();
            if (!Util.isValidEmail(identifier)) {
                log.warn("Invalid email format received: {}", identifier);
                return "redirect:/login/email?error=invalid_email";
            }
        } else {
            identifier = Util.normalizePhoneNumber(identifier);
            if (identifier == null) {
                log.warn("Invalid phone number format received: {}", identifier);
                return "redirect:/login/phone?error=invalid_phone";
            }
        }

        // 2. Check if user exists
        if (isEmail && !userService.checkEmailExists(identifier)) {
            log.info("Unregistered email attempted OTP: {}", identifier);
            return "redirect:/register?email=" + URLEncoder.encode(identifier, StandardCharsets.UTF_8);
        } else if (!isEmail && !userService.checkPhoneExists(identifier)) {
            log.info("Unregistered phone attempted OTP: {}", identifier);
            return "redirect:/register?phone=" + URLEncoder.encode(identifier, StandardCharsets.UTF_8);
        }

        // 3. Rate limiting
        if (!otpRateLimiter.isAllowed(rateLimitKey)) {
            log.warn("OTP rate limit exceeded for IP={} and {}", clientIp, type.toLowerCase());
            return "redirect:/login/" + type.toLowerCase() + "?error=too_many_requests";
        }

        // 4. Prevent OTP resend within 60 seconds from same session
        Long lastSent = (Long) session.getAttribute("otpSentTime");
        if (lastSent != null && System.currentTimeMillis() - lastSent < 60_000) {
            log.info("OTP recently sent (within 60s) to {}={} from session", type.toLowerCase(), identifier);
            return "redirect:/login/" + type.toLowerCase() + "?error=otp_recently_sent";
        }

        // 5. Generate and send OTP
        otpService.generateAndSendOtp(identifier, isEmail ? OtpType.EMAIL : OtpType.PHONE, clientIp);
        log.info("OTP sent to {}={} from IP={}", type.toLowerCase(), identifier, clientIp);

        // 6. Store session metadata
        session.setAttribute("otp" + type, identifier);
        session.setAttribute("otpSentTime", System.currentTimeMillis());
        session.setAttribute("otpType", isEmail ? OtpType.EMAIL.name() : OtpType.PHONE.name());

        // 7. Show OTP page
        model.addAttribute("recipient", identifier);
        model.addAttribute("otpType", isEmail ? OtpType.EMAIL.name() : OtpType.PHONE.name());

        return "otp-verification";
    }


}

