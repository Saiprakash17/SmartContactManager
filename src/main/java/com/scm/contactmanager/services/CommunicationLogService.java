package com.scm.contactmanager.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.scm.contactmanager.dtos.CommunicationLogResponse;
import com.scm.contactmanager.dtos.CreateCommunicationLogRequest;
import com.scm.contactmanager.entities.CommunicationLog;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;

public interface CommunicationLogService {

    /**
     * Create a new communication log entry for a contact
     */
    CommunicationLog createCommunicationLog(Long contactId, CreateCommunicationLogRequest request, User user);

    /**
     * Get all communication logs for a specific contact
     */
    List<CommunicationLogResponse> getContactCommunications(Contact contact);

    /**
     * Get paginated communication logs for a specific contact
     */
    Page<CommunicationLogResponse> getContactCommunications(Contact contact, Pageable pageable);

    /**
     * Get communication logs for a user within a date range
     */
    List<CommunicationLogResponse> getCommunicationsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get communication logs that need follow-up
     */
    List<CommunicationLogResponse> getPendingFollowups(User user);

    /**
     * Update a communication log
     */
    CommunicationLog updateCommunicationLog(Long logId, CreateCommunicationLogRequest request, User user);

    /**
     * Delete a communication log
     */
    void deleteCommunicationLog(Long logId, User user);

    /**
     * Get communication log by ID
     */
    CommunicationLog getCommunicationLogById(Long logId);

    /**
     * Get recent communications for a contact (last N days)
     */
    List<CommunicationLogResponse> getRecentCommunications(Contact contact, int days);

    /**
     * Get communication count for a contact
     */
    Long getCommunicationCount(Contact contact);
}
