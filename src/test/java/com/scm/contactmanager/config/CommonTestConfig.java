package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.scm.contactmanager.security.SecurityAuditLogger;
import com.scm.contactmanager.security.RateLimiter;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class CommonTestConfig {
    
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
    
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public SecurityAuditLogger securityAuditLogger() {
        return mock(SecurityAuditLogger.class);
    }

    @Bean
    @Primary
    public RateLimiter rateLimiter() {
        return mock(RateLimiter.class);
    }

}