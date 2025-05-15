package com.mshzidan.guard.controller;

import com.mshzidan.guard.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registrationRequest"));
    }


    @Test
    void shouldRejectInvalidEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "bad-email")
                        .param("phone", "+201234567890")
                        .param("password", "StrongPass123!")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Invalid email format."));
    }
    @Test
    void shouldRejectInvalidPhone() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("phone", "notaphone")
                        .param("password", "StrongPass123!")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Invalid phone number format."));
    }

    @Test
    void shouldRejectWeakPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("phone", "+201234567890")
                        .param("password", "123")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Password does not meet the complexity requirements."));
    }

    @Test
    void shouldRejectExistingEmail() throws Exception {
        Mockito.when(userService.checkEmailExists("user@example.com")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("phone", "+201234567890")
                        .param("password", "StrongPass123!")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Email is already in use."));
    }


    @Test
    void shouldRegisterSuccessfully() throws Exception {
        Mockito.when(userService.checkEmailExists("user@example.com")).thenReturn(false);
        Mockito.when(userService.checkPhoneExists("+201234567890")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("StrongPass123!")).thenReturn("encodedPass");

        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("phone", "+201234567890")
                        .param("password", "StrongPass123!")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }


    @Test
    void shouldHandleUnexpectedException() throws Exception {
        Mockito.when(userService.checkEmailExists("user@example.com")).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("phone", "+201234567890")
                        .param("password", "StrongPass123!")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Registration failed. Please try again later."));
    }


    @Test
    void shouldRejectExistingPhoneNumber() throws Exception {
        Mockito.when(userService.checkEmailExists("user@example.com")).thenReturn(false);
        Mockito.when(userService.checkPhoneExists("+201234567890")).thenReturn(true); // Simulate phone in use

        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("phone", "+201234567890")
                        .param("password", "StrongPass123!")
                        .param("username", "user1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Phone number is already in use."));
    }




}