package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        var userDetails = User.withUsername("test@example.com")
            .password(passwordEncoder().encode("testpassword"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers("/css/**", "/js/**", "/img/**"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .contentSecurityPolicy(csp -> 
                    csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'")))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/", "/register", "/about", "/login", 
                    "/img/**", "/css/**", "/js/**", "/process_login",
                    "/process-register", "/user/verify/**", "/do_register",
                    "/authenticate", "/signin", "/signup",
                    "/forgot", "/reset-password/**").permitAll()
                .requestMatchers("/user/**").hasRole("USER")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/process_login")
                .defaultSuccessUrl("/user/index")
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .permitAll())
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired"))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, authException) -> 
                    response.sendRedirect("/login")));

        return http.build();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
