package com.scm.contactmanager.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.scm.contactmanager.helper.SessionHelper;
import com.scm.contactmanager.repositories.AddressRepo;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.scm.contactmanager.repositories.PasswordResetTokenRepo;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.EmailService;

@TestConfiguration
public class TestConfig {

    @Bean
    public UserRepo userRepo() {
        return Mockito.mock(UserRepo.class);
    }

    @Bean
    public ContactRepo contactRepo() {
        return Mockito.mock(ContactRepo.class);
    }

    @Bean
    public AddressRepo addressRepo() {
        return Mockito.mock(AddressRepo.class);
    }

    @Bean
    public PasswordResetTokenRepo passwordResetTokenRepo() {
        return Mockito.mock(PasswordResetTokenRepo.class);
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public PasswordEncoder passwordEncoder() {
        return Mockito.mock(PasswordEncoder.class);
    }

    @Bean
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

    @Bean
    public SessionHelper sessionHelper() {
        return Mockito.mock(SessionHelper.class);
    }

    @Bean
    public ImageService imageService() {
        return Mockito.mock(ImageService.class);
    }

    @Bean
    public QRCodeGeneratorService qrCodeGeneratorService() {
        return Mockito.mock(QRCodeGeneratorService.class);
    }
}
