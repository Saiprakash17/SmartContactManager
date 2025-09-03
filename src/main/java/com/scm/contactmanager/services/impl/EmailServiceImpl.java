package com.scm.contactmanager.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.services.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender eMailSender;

    @Value("${spring.mail.properties.domain_name}")
    private String domainName;

    @Value("${spring.mail.properties.feedback_to_email}")
    private String feedbackToEmail;

    @Value("${spring.mail.properties.feedback_from_email}")
    private String feedbackFromEmail;

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(domainName);
        eMailSender.send(message);
    }

    @Override
    public void sendFeedbackEmail(String from, String subject, String body) {
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
