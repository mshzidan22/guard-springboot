package com.mshzidan.guard.security.services;

import com.mshzidan.guard.repository.AuthProviderRepository;
import com.mshzidan.guard.repository.UserRepository;
import com.mshzidan.guard.security.entites.AuthProvider;
import com.mshzidan.guard.security.entites.User;
import com.mshzidan.guard.security.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String providerUserId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (email == null || providerUserId == null) {
            throw new OAuth2AuthenticationException("Missing required Google user attributes");
        }

        // Find or create User
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setPassword(PasswordUtil.generateHashedPassword());
            return userRepository.save(newUser);
        });

        // Link AuthProvider if not already linked
        authProviderRepository.findByUserAndProvider(user, "GOOGLE")
                .orElseGet(() -> {
                    AuthProvider authProvider = new AuthProvider();
                    authProvider.setUser(user);
                    authProvider.setProvider("GOOGLE");
                    authProvider.setProviderUserId(providerUserId);
                    return authProviderRepository.save(authProvider);
                });
        //Get System Authorities
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
