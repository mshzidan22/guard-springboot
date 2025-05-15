package com.mshzidan.guard.security.services;

import com.mshzidan.guard.security.components.CustomUserDetails;
import com.mshzidan.guard.security.entites.User;
import com.mshzidan.guard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final  UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("Can't find username = " + username));
        return new CustomUserDetails(user);
    }
}
