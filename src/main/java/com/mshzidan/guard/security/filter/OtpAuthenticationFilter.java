package com.mshzidan.guard.security.filter;


import com.mshzidan.guard.security.entites.OtpType;
import com.mshzidan.guard.security.token.OtpAuthenticationToken;
import com.mshzidan.guard.security.utils.Util;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RequiredArgsConstructor
public class OtpAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final String validationURL;
    private final String successUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String recipient = Optional.ofNullable(request.getParameter("recipient")).map(String::trim).orElse(null);
        String otpCode = Optional.ofNullable(request.getParameter("otp")).map(String::trim).orElse(null);
        String ipAddress = Util.getClientIp(request);

        if (recipient == null || otpCode == null) {
            response.sendRedirect("/login/email?error=Invalid+Credentials");
            return;
        }

        OtpType type;
        try {
            type = Util.getRecipientType(recipient);
            OtpAuthenticationToken authRequest = new OtpAuthenticationToken(recipient, otpCode, type, ipAddress);
            Authentication result = authenticationManager.authenticate(authRequest);
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(result);
            request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            response.sendRedirect(successUrl);

        } catch (AuthenticationException ex) {
            redirectWithError(response, Util.getRecipientType(recipient), "otp_invalid");
        } catch (IllegalArgumentException ex) {
            redirectWithError(response, Util.getRecipientType(recipient), "bad_request");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher(validationURL, "POST");
        return !requestMatcher.matches(request);
    }

    private void redirectWithError(HttpServletResponse response, OtpType type, String error) throws IOException {
        String url = (type == OtpType.EMAIL) ? "/login/email" : "/login/phone";
        response.sendRedirect(url + "?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
    }

}
