package com.scm.contactmanager.services;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    public void sendFeedbackEmail(String to, String subject, String body);
}
