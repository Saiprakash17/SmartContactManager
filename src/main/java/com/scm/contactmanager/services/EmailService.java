package com.scm.contactmanager.services;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    // void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath);

    // void sendEmailWithInlineImage(String to, String subject, String body, String imagePath);

    // void sendEmailWithTemplate(String to, String subject, String templateName, Object model);
}
