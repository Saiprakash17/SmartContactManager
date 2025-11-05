package com.scm.contactmanager.config;

import com.scm.contactmanager.services.*;
import com.scm.contactmanager.services.impl.*;
import com.scm.contactmanager.repositories.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class TestApiConfig implements WebMvcConfigurer {
    
    @Bean
    @Primary
    public UserService userService() {
        return Mockito.mock(UserService.class);
    }
    
    @Bean
    @Primary
    public QRCodeGeneratorService qrCodeGeneratorService() {
        return Mockito.mock(QRCodeGeneratorService.class);
    }
    
    @Bean
    @Primary
    public ImageService imageService() {
        return Mockito.mock(ImageService.class);
    }
    
    @Bean
    @Primary
    public ContactRepo contactRepo() {
        return Mockito.mock(ContactRepo.class);
    }
    
    @Bean
    @Primary
    public ContactService contactService() {
        ContactServiceImpl service = new ContactServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "userService", userService());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "qrCodeGeneratorService", qrCodeGeneratorService());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "imageService", imageService());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "contactRepo", contactRepo());
        return service;
    }
}