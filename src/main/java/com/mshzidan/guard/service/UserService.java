package com.mshzidan.guard.service;

import com.mshzidan.guard.repository.UserRepository;
import com.mshzidan.guard.security.entites.User;
import com.mshzidan.guard.security.exception.UserAlreadyExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public void registerNewUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered.");
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new UserAlreadyExistsException("Phone number already registered.");
        }

        user.setEnabled(true);
        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public boolean checkPhoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User getUserByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + phone));
    }
}
