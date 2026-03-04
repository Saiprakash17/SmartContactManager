package com.scm.contactmanager.services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender eMailSender;
    private final String activeProfile;
    private final String domainName;
    private final String feedbackToEmail;
    private final String feedbackFromEmail;
    private final String fromEmail;

    public EmailServiceImpl(JavaMailSender eMailSender,
            @Value("${spring.profiles.active:test}") String activeProfile,
            @Value("${spring.mail.properties.domain_name}") String domainName,
            @Value("${spring.mail.properties.feedback_to_email}") String feedbackToEmail,
            @Value("${spring.mail.properties.feedback_from_email}") String feedbackFromEmail,
            @Value("${spring.mail.properties.from_email}") String fromEmail) {
        this.eMailSender = eMailSender;
        this.activeProfile = activeProfile;
        this.domainName = domainName;
        this.feedbackToEmail = feedbackToEmail;
        this.feedbackFromEmail = feedbackFromEmail;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.trim().isEmpty()) {
            return;
        }
        // Skip sending email in test profile
        if ("test".equals(activeProfile)) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(fromEmail);
        eMailSender.send(message);
    }

    @Override
    public void sendFeedbackEmail(String from, String subject, String body) {
        // Skip sending email in test profile
        if ("test".equals(activeProfile)) {
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        StringBuilder content = new StringBuilder();
        content.append("Feedback From: ").append(from).append("\n\n");
        content.append(body);
        message.setTo(feedbackToEmail);
        message.setSubject(subject);
        message.setText(content.toString());
        message.setFrom(feedbackFromEmail);
        eMailSender.send(message);
    }

}
