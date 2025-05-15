package com.mshzidan.guard.controller;

import com.mshzidan.guard.security.dto.RegistrationRequest;
import com.mshzidan.guard.security.entites.User;
import com.mshzidan.guard.security.utils.PasswordUtil;
import com.mshzidan.guard.security.utils.Util;
import com.mshzidan.guard.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // Show Registration Form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }

    // Handle Registration Form Submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegistrationRequest request, Model model) {
        try {
            // Input Validation
            if (!Util.isValidEmail(request.getEmail())) {
                model.addAttribute("error", "Invalid email format.");
                return "register";
            }
            if (!Util.isValidPhoneNumber(request.getPhone())) {
                model.addAttribute("error", "Invalid phone number format.");
                return "register";
            }
            if (!PasswordUtil.isValidPassword(request.getPassword())) {
                model.addAttribute("error", "Password does not meet the complexity requirements.");
                return "register";
            }

            // Check if email or phone already exists
            if (userService.checkEmailExists(request.getEmail())) {
                model.addAttribute("error", "Email is already in use.");
                return "register";
            }

            if (userService.checkPhoneExists(request.getPhone())) {
                model.addAttribute("error", "Phone number is already in use.");
                return "register";
            }

            // Create and Save User
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(Util.formatPhoneNumber(request.getPhone()));

            userService.registerNewUser(user);

            // Log registration success
            log.info("User registered successfully: {}", request.getEmail());

            // Add success message and clear form
            model.addAttribute("success", "Registration successful!");
            model.addAttribute("registrationRequest", new RegistrationRequest());

            // Optionally, redirect to login page
            return "redirect:/login";
        } catch (Exception e) {
            // Log error
            log.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage());

            // Show error message
            model.addAttribute("error", "Registration failed. Please try again later.");
            return "register";
        }
    }
}
