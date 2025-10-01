package com.scm.contactmanager.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.cloudinary.Cloudinary;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class CommonTestConfig {
    
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "test_cloud");
        config.put("api_key", "test_key");
        config.put("api_secret", "test_secret");
        return new Cloudinary(config);
    }

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://unpkg.com; " +
                        "style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net; " +
                        "img-src 'self' data: blob: https://res.cloudinary.com; " +
                        "frame-src 'self'"
                    )
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/user/**").authenticated()
                .anyRequest().permitAll()
            )
            .securityContext(securityContext -> securityContext
                .requireExplicitSave(true)
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getSession(false) != null) {
                        request.getSession().invalidate();
                    }
                    response.setStatus(HttpServletResponse.SC_FOUND);
                    response.sendRedirect("http://localhost/login");
                })
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureUrl("/login?error=true")
                .successHandler((request, response, authentication) -> {
                    // Get old session ID for testing
                    String oldSessionId = request.getSession(false) != null 
                        ? request.getSession(false).getId() 
                        : null;

                    // Invalidate old session if exists
                    if (request.getSession(false) != null) {
                        request.getSession(false).invalidate();
                    }

                    // Create new session
                    request.getSession(true);
                    String newSessionId = request.getSession(false).getId();

                    // Store session IDs for validation
                    if (oldSessionId != null) {
                        request.getSession().setAttribute("oldSessionId", oldSessionId);
                        request.getSession().setAttribute("newSessionId", newSessionId);
                    }

                    // Set redirect status and send redirect
                    response.setStatus(HttpServletResponse.SC_FOUND);
                    response.sendRedirect("/user/dashboard");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                session.sessionFixation(fixation -> fixation.newSession());
                session.invalidSessionStrategy((request, response) -> {
                    response.setStatus(HttpServletResponse.SC_FOUND);
                    response.sendRedirect("http://localhost/login");
                });
                session.maximumSessions(1)
                      .maxSessionsPreventsLogin(true)
                      .expiredUrl("http://localhost/login?expired=true");
            })
            .securityContext(securityContext -> securityContext
                .requireExplicitSave(false)
            );

        return http.build();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public SecurityCustomUserDeatilsService userDetailsService(UserRepo userRepo) {
        return new SecurityCustomUserDeatilsService(userRepo);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService(null));
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return authentication -> authenticationProvider().authenticate(authentication);
    }
}