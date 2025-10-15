package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.cloudinary.Cloudinary;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class CommonTestConfig {
    
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
    
    @Bean
    @Primary
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "test_cloud");
        config.put("api_key", "test_key");
        config.put("api_secret", "test_secret");
        return new Cloudinary(config);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public SecurityCustomUserDeatilsService customUserDetailsService(UserRepo userRepo) {
        SecurityCustomUserDeatilsService service = new SecurityCustomUserDeatilsService(userRepo);
        return service;
    }
}