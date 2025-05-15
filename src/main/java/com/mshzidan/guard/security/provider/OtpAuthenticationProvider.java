package com.mshzidan.guard.security.provider;

import com.mshzidan.guard.security.entites.OtpType;
import com.mshzidan.guard.security.entites.User;
import com.mshzidan.guard.security.otp.OtpService;
import com.mshzidan.guard.security.token.OtpAuthenticationToken;
import com.mshzidan.guard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import static com.mshzidan.guard.security.utils.Util.mapRolesToAuthorities;


@Component
@RequiredArgsConstructor
public class OtpAuthenticationProvider implements AuthenticationProvider {

    private final OtpService otpService;
    private final UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OtpAuthenticationToken otpAuthenticationToken = (OtpAuthenticationToken) authentication;
        String recipient = otpAuthenticationToken.getPrincipal().toString();
        String otpCode = authentication.getCredentials().toString();
        OtpType otpType = otpAuthenticationToken.getOtpType();
        String ipAddress = otpAuthenticationToken.getIpAddress();

        boolean verified = otpService.verifyOtp(recipient, otpCode, otpType, ipAddress);


        if (verified) {
            User user = null;
            if (otpType == OtpType.EMAIL) {
                user = userService.getUserByEmail(recipient);
            } else if (otpType == OtpType.PHONE) {
                user = userService.getUserByPhone(recipient);
            } else {
                throw new IllegalArgumentException("Unsupported OTP type: " + otpType);
            }
            if (user == null) {
                throw new UsernameNotFoundException("User not found for recipient: " + recipient);
            }
            return new OtpAuthenticationToken(user.getUsername(), mapRolesToAuthorities(user.getRoles()));
        }
        throw new BadCredentialsException("Invalid OTP or user not found.");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
