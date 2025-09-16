package com.scm.contactmanager.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cloudinary.Cloudinary;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@EnableWebMvc
@ComponentScan(basePackages = {
    "com.scm.contactmanager.controllers",
    "com.scm.contactmanager.services"
})
@Import({
    TestSecurityConfig.class,
    TestMailConfig.class,
    TestDataSourceConfig.class
})
@Profile("test")
public class TestConfig implements WebMvcConfigurer {
    
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "test_cloud");
        config.put("api_key", "test_key");
        config.put("api_secret", "test_secret");
        return new Cloudinary(config);
    }

    @Bean
    public UserRepo userRepo() {
        return Mockito.mock(UserRepo.class);
    }

    @Bean
    public ContactRepo contactRepo() {
        return Mockito.mock(ContactRepo.class);
    }

    @Bean
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

    @Bean
    public ContactService contactService() {
        return Mockito.mock(ContactService.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    public SecurityCustomUserDeatilsService userDetailsService() {
        return Mockito.mock(SecurityCustomUserDeatilsService.class);
    }
}
