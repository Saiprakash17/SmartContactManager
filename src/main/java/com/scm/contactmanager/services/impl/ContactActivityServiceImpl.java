package com.scm.contactmanager.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactActivity;
import com.scm.contactmanager.entities.ContactActivity.ActivityType;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.ContactActivityRepo;
import com.scm.contactmanager.services.ContactActivityService;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class ContactActivityServiceImpl implements ContactActivityService {

    @Autowired
    private ContactActivityRepo activityRepository;

    @Override
    public void logActivity(Contact contact, User user, ActivityType type,
            String description, HttpServletRequest request) {
        
        String ipAddress = getClientIp(request);
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        ContactActivity activity = ContactActivity.builder()
            .contact(contact)
            .user(user)
            .activityType(type)
            .description(description)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        activityRepository.save(activity);
    }

    @Override
    public List<ContactActivity> getContactTimeline(Contact contact) {
        return activityRepository.findByContactOrderByTimestampDesc(contact);
    }

    @Override
    public List<ContactActivity> getRecentActivities(User user, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return activityRepository.findByUserAndTimestampAfter(user, startDate);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
