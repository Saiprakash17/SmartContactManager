package com.scm.contactmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityCustomUserDeatilsService userDetailsService;

    @Autowired
    private AuthFailtureHandler authFailtureHandler;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // Configure CSRF protection
            .csrf(csrf -> csrf.ignoringRequestMatchers("/css/**", "/js/**", "/img/**"))
            // Configure authorization rules
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/signup", "/login", "/css/**", "/js/**", "/img/**").permitAll()
                    .requestMatchers("/user/**").authenticated()
                    .anyRequest().permitAll()
            )
            // Configure login
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/authenticate")
                    .defaultSuccessUrl("/user/dashboard", true)
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .failureHandler(authFailtureHandler)
            )
            // Configure logout
            .logout(logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
            )
            // Configure session management
            .sessionManagement(session -> 
                session
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(true)
            )
            // Configure security headers
            .headers(headers -> 
                headers
                    .xssProtection(xss -> {})
                    .contentTypeOptions(cto -> {})
                    .frameOptions(frame -> frame.deny())
                    .httpStrictTransportSecurity(hsts -> 
                        hsts
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000)
                    )
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; frame-ancestors 'self'; script-src 'self' 'unsafe-inline'")
                    )
            );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
