package com.mshzidan.guard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class HomePageTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginWithoutCredentials() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithoutCredentialsForHomePage() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithoutCredentialsForNoPage() throws Exception {
        mockMvc.perform(get(""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithInvalidCredentials() throws Exception {
        mockMvc.perform(get("/")
                        .with(httpBasic("ADMIN","4352345")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithValidCredentials() throws Exception {
        mockMvc.perform(get("/")
                        .with(httpBasic("ADMIN","123"))) //Comes from database
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "Ahmed", roles = {"USER"})   //Assumes an authenticated user in the security context.
    void testProtectedEndpointWithUser() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello <b>Ahmed")))
                .andExpect(content().string(containsString("ROLE_USER")));

    }

}
