/*
 * File:        SecurityConfig.java
 * Module:      Admin security
 * Purpose:     Protects admin routes, CSRF and logout, and provides the BCrypt encoder (NFR-06, NFR-07, NFR-09).
 * Responsible: Felix Joshua H. Abing
 */
package com.paayo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/** Spring Security rules. The login itself is handled by AdminController. */
@Configuration
public class SecurityConfig {
    private static final String LOGIN_URL = "/admin/login";

    /** Defines which routes are public and which need an authenticated administrator. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(LOGIN_URL, "/admin/forgot-password", "/admin/reset-password").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(LOGIN_URL))
                        .accessDeniedHandler(accessDeniedHandler()))
                // A cookie token survives the restarts and sleeps of a free Render instance; a session token would not.
                .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
                .logout(logout -> logout.logoutUrl("/admin/logout").logoutSuccessUrl("/"))
                .build();
    }

    /** Stores the login in the HTTP session. */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Hashes passwords with BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // A signed-out admin POST fails the CSRF check first; NFR-07 still wants the login page instead of a 403.
    private static AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandler forbidden = new AccessDeniedHandlerImpl();
        return (request, response, denied) -> {
            boolean signedOut = SecurityContextHolder.getContext().getAuthentication() == null;
            if (signedOut && request.getRequestURI().startsWith(request.getContextPath() + "/admin/")) {
                response.sendRedirect(LOGIN_URL);
            } else {
                forbidden.handle(request, response, denied);
            }
        };
    }
}
