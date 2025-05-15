package com.mshzidan.guard.security.config;

import com.mshzidan.guard.security.filter.OtpAuthenticationFilter;
import com.mshzidan.guard.security.otp.OtpService;
import com.mshzidan.guard.security.provider.OtpAuthenticationProvider;
import com.mshzidan.guard.security.services.AppUserDetailsService;
import com.mshzidan.guard.security.services.GoogleOidcUserService;
import com.mshzidan.guard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final AppUserDetailsService appUserDetailsService;
    private final OtpService optService;
    private final UserService userService;
    private final OtpAuthenticationProvider otpAuthenticationProvider;
    private final GoogleOidcUserService googleOAuth2UserService;
    @Value("${otp.validation.url:/login/validate-otp}")
    private String otpValidationUrl;
    @Value("${otp.success.url:/home}")
    private String successUrl;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/register").permitAll()
                            .requestMatchers("/login", "/login/**").permitAll()
                            .requestMatchers("/css/**", "/js/**").permitAll()
                            .requestMatchers("/error/**").permitAll()
                            .requestMatchers("/auth/**").permitAll()
                            .anyRequest().authenticated();

                }).formLogin(form ->
                        form.loginPage("/login").defaultSuccessUrl(successUrl,true)
                )
                .httpBasic(Customizer.withDefaults())

                .rememberMe(k ->
                        k.key("secretKey")
                                .tokenValiditySeconds(7 * 24 * 60 * 60)
                                .rememberMeParameter("remember-me")
                                .userDetailsService(appUserDetailsService)
                )
                .oauth2Login(oauth ->
                                 oauth.loginPage("/login")
                                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(googleOAuth2UserService))
                                .defaultSuccessUrl(successUrl)

                )
                .addFilterBefore(otpAuthenticationFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(otpAuthenticationProvider);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(appUserDetailsService);
        return provider;
    }

    @Bean
    public OtpAuthenticationFilter otpAuthenticationFilter(AuthenticationManager authManager) {
        return new OtpAuthenticationFilter(authManager, otpValidationUrl, successUrl);
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(otpAuthenticationProvider);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
        return authenticationManagerBuilder.build();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }


}
