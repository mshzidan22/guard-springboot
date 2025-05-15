package com.mshzidan.guard.controller;

import com.mshzidan.guard.security.otp.OtpRateLimiter;
import com.mshzidan.guard.security.otp.OtpService;
import com.mshzidan.guard.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OtpService otpService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OtpRateLimiter otpRateLimiter;

    @Test
    void shouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void shouldReturnEmailEntryPage() throws Exception {
        mockMvc.perform(get("/login/email"))
                .andExpect(status().isOk())
                .andExpect(view().name("email-entry"));
    }

    @Test
    void shouldReturnPhoneEntryPage() throws Exception {
        mockMvc.perform(get("/login/phone"))
                .andExpect(status().isOk())
                .andExpect(view().name("phone-entry"));
    }

    @Test
    void shouldRejectInvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/login/email/send-otp")
                        .param("email", "NotAcceptableEmail")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login/email?error=invalid_email"));
    }

    @Test
    void shouldRedirectIfEmailDoesNotExist() throws Exception {
        String email = "NotExist@example.com";

        Mockito.when(userService.checkEmailExists(email)).thenReturn(false);

        mockMvc.perform(post("/login/email/send-otp")
                        .param("email", email)
                        .with(csrf()))
                .andExpect(redirectedUrlPattern("/register?email=*"));
    }

    @Test
    void shouldRedirectIfPhoneInvalid() throws Exception {
        mockMvc.perform(post("/login/phone/send-otp")
                        .param("phone", "abc123")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login/phone?error=invalid_phone"));
    }

    @Test
    void shouldRedirectWhenRateLimitExceeded() throws Exception {
        String email = "test@example.com";

        Mockito.when(userService.checkEmailExists(email)).thenReturn(true);
        Mockito.when(otpRateLimiter.isAllowed(anyString())).thenReturn(false);

        mockMvc.perform(post("/login/email/send-otp")
                        .param("email", email)
                        .with(csrf())
                        .session(new MockHttpSession()))
                .andExpect(redirectedUrl("/login/email?error=too_many_requests"));
    }

    @Test
    void shouldRejectOtpIfResentWithin60Seconds() throws Exception {
        String email = "test@example.com";
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("otpSentTime", System.currentTimeMillis()); // OTP just sent

        Mockito.when(userService.checkEmailExists(email)).thenReturn(true);
        Mockito.when(otpRateLimiter.isAllowed(anyString())).thenReturn(true);

        mockMvc.perform(post("/login/email/send-otp")
                        .param("email", email)
                        .with(csrf())
                        .session(session))
                .andExpect(redirectedUrl("/login/email?error=otp_recently_sent"));
    }


    @Test
    void shouldSendOtpAndRedirectToOtpPage() throws Exception {
        String email = "test@example.com";
        MockHttpSession session = new MockHttpSession();

        Mockito.when(userService.checkEmailExists(email)).thenReturn(true);
        Mockito.when(otpRateLimiter.isAllowed(anyString())).thenReturn(true);

        mockMvc.perform(post("/login/email/send-otp")
                        .param("email", email)
                        .with(csrf())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("otp-verification"))
                .andExpect(model().attribute("recipient", email))
                .andExpect(model().attribute("otpType", "EMAIL"));

        assertEquals(email, session.getAttribute("otpemail"));
        assertEquals("EMAIL", session.getAttribute("otpType"));
    }

    @Test
    void shouldRedirectToRegisterIfEmailNotExists() throws Exception {
        Mockito.when(userService.checkEmailExists("notfound@example.com")).thenReturn(false);

        mockMvc.perform(post("/login/email/send-otp")
                        .param("email", "notfound@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register?email=notfound%40example.com"));
    }


}



