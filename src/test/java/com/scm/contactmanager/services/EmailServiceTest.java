package com.scm.contactmanager.services;

import com.scm.contactmanager.services.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 1. Replace @SpringBootTest with @ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // 2. Manually set the value for the @Value-annotated field
        // This simulates what Spring does when it reads application.properties
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
    }

    @Test
    void shouldSendEmail() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail(to, subject, text);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldHandleEmptyRecipient() {
        String subject = "Test Subject";
        String text = "Test Message";

        emailService.sendEmail("", subject, text);

        // Verify that the send method is never called if the recipient is empty
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldHandleLongMessage() {
        String to = "test@example.com";
        String subject = "Test Subject";
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longText.append("Test Message ");
        }

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail(to, subject, longText.toString());

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}