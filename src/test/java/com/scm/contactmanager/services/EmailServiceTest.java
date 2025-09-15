package com.scm.contactmanager.services;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.ActiveProfiles;

import com.scm.contactmanager.services.impl.EmailServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

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
