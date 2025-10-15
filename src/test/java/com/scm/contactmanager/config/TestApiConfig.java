package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class TestApiConfig implements WebMvcConfigurer {
    
    @Bean
    @Primary
    public com.scm.contactmanager.services.ContactService contactService(
            com.scm.contactmanager.services.UserService userService,
            com.scm.contactmanager.services.QRCodeGeneratorService qrCodeGeneratorService,
            com.scm.contactmanager.services.ImageService imageService,
            com.scm.contactmanager.repositories.ContactRepo contactRepo
    ) {
        com.scm.contactmanager.services.impl.ContactServiceImpl service = new com.scm.contactmanager.services.impl.ContactServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "userService", userService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "qrCodeGeneratorService", qrCodeGeneratorService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "imageService", imageService);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "contactRepo", contactRepo);
        return service;
    }
}