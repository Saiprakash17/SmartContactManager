package com.scm.contactmanager.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.dtos.CommunicationLogResponse;
import com.scm.contactmanager.dtos.CreateCommunicationLogRequest;
import com.scm.contactmanager.entities.CommunicationLog;
import com.scm.contactmanager.entities.CommunicationLog.CommunicationType;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactActivity;
import com.scm.contactmanager.entities.ContactActivity.ActivityType;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.CommunicationLogRepo;
import com.scm.contactmanager.repositories.ContactActivityRepo;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.CommunicationLogService;

@Service
@Transactional
public class CommunicationLogServiceImpl implements CommunicationLogService {

    private final CommunicationLogRepo communicationLogRepository;
    private final ContactRepo contactRepository;
    private final ContactActivityRepo contactActivityRepository;

    public CommunicationLogServiceImpl(CommunicationLogRepo communicationLogRepository,
                                     ContactRepo contactRepository,
                                     ContactActivityRepo contactActivityRepository) {
        this.communicationLogRepository = communicationLogRepository;
        this.contactRepository = contactRepository;
        this.contactActivityRepository = contactActivityRepository;
    }

    @Override
    public CommunicationLog createCommunicationLog(Long contactId, CreateCommunicationLogRequest request, User user) {
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));

        CommunicationType type = CommunicationType.valueOf(request.getType().toUpperCase());

        CommunicationLog log = CommunicationLog.builder()
            .contact(contact)
            .user(user)
            .type(type)
            .notes(request.getNotes())
            .outcome(request.getOutcome())
            .nextFollowUp(request.getNextFollowUp())
            .build();

        CommunicationLog savedLog = communicationLogRepository.save(log);

        // Log the activity
        ContactActivity activity = ContactActivity.builder()
            .contact(contact)
            .user(user)
            .activityType(ActivityType.COMMUNICATION_LOGGED)
            .description("Logged " + type.toString().toLowerCase() + " communication")
            .build();
        contactActivityRepository.save(activity);

        return savedLog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationLogResponse> getContactCommunications(Contact contact) {
        List<CommunicationLog> logs = communicationLogRepository.findByContactOrderByTimestampDesc(contact);
        return logs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunicationLogResponse> getContactCommunications(Contact contact, Pageable pageable) {
        Page<CommunicationLog> logs = communicationLogRepository.findByContactOrderByTimestampDesc(contact, pageable);
        return logs.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationLogResponse> getCommunicationsByDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        List<CommunicationLog> logs = communicationLogRepository.findByUserAndDateRange(user, startDate, endDate);
        return logs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationLogResponse> getPendingFollowups(User user) {
        List<CommunicationLog> logs = communicationLogRepository.findPendingFollowups(user);
        return logs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public CommunicationLog updateCommunicationLog(Long logId, CreateCommunicationLogRequest request, User user) {
        CommunicationLog log = communicationLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Communication log not found"));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        log.setType(CommunicationType.valueOf(request.getType().toUpperCase()));
        log.setNotes(request.getNotes());
        log.setOutcome(request.getOutcome());
        log.setNextFollowUp(request.getNextFollowUp());

        return communicationLogRepository.save(log);
    }

    @Override
    public void deleteCommunicationLog(Long logId, User user) {
        CommunicationLog log = communicationLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Communication log not found"));

        if (!log.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        communicationLogRepository.delete(log);
    }

    @Override
    @Transactional(readOnly = true)
    public CommunicationLog getCommunicationLogById(Long logId) {
        return communicationLogRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Communication log not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunicationLogResponse> getRecentCommunications(Contact contact, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<CommunicationLog> logs = communicationLogRepository.findRecentCommunications(contact, startDate);
        return logs.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommunicationCount(Contact contact) {
        return communicationLogRepository.countByContact(contact);
    }

    private CommunicationLogResponse convertToResponse(CommunicationLog log) {
        return CommunicationLogResponse.builder()
            .id(log.getId())
            .contactId(log.getContact().getId())
            .contactName(log.getContact().getName())
            .type(log.getType().toString())
            .notes(log.getNotes())
            .outcome(log.getOutcome())
            .timestamp(log.getTimestamp())
            .nextFollowUp(log.getNextFollowUp())
            .lastModified(log.getLastModified())
            .build();
    }
}
