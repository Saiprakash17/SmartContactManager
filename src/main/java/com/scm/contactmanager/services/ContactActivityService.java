package com.scm.contactmanager.services;

import java.util.List;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactActivity;
import com.scm.contactmanager.entities.ContactActivity.ActivityType;
import com.scm.contactmanager.entities.User;
import jakarta.servlet.http.HttpServletRequest;

public interface ContactActivityService {
    void logActivity(Contact contact, User user, ActivityType type, 
                    String description, HttpServletRequest request);

    List<ContactActivity> getContactTimeline(Contact contact);

    List<ContactActivity> getRecentActivities(User user, int days);
}
