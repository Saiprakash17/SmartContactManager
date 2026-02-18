package com.scm.contactmanager.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactActivity.ActivityType;
import com.scm.contactmanager.entities.ContactTag;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.BulkActionRequest;
import com.scm.contactmanager.payloads.BulkActionResponse;
import com.scm.contactmanager.payloads.BulkActionType;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.repositories.ContactTagRepo;
import com.scm.contactmanager.services.BulkActionService;
import com.scm.contactmanager.services.ContactActivityService;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class BulkActionServiceImpl implements BulkActionService {

    @Autowired
    private ContactRepo contactRepository;

    @Autowired
    private ContactTagRepo tagRepository;

    @Autowired
    private ContactActivityService activityService;

    @Override
    public BulkActionResponse performBulkAction(BulkActionRequest request, User user,
            HttpServletRequest httpRequest) {

        List<Contact> contacts = contactRepository.findByIdInAndUser(request.getContactIds(), user);

        if (contacts.isEmpty()) {
            throw new RuntimeException("No valid contacts found for bulk operation");
        }

        int processed = 0;
        int failed = 0;
        String message = "";

        try {
            switch (request.getActionType()) {
                case DELETE:
                    processed = performDelete(contacts, user, httpRequest);
                    message = "Deleted " + processed + " contacts";
                    break;
                case ADD_TAG:
                    processed = performAddTag(contacts, (Long) request.getActionData());
                    message = "Added tag to " + processed + " contacts";
                    break;
                case REMOVE_TAG:
                    processed = performRemoveTag(contacts, (Long) request.getActionData());
                    message = "Removed tag from " + processed + " contacts";
                    break;
                case MARK_FAVORITE:
                    processed = performMarkFavorite(contacts, true);
                    message = "Marked " + processed + " contacts as favorite";
                    break;
                case UNMARK_FAVORITE:
                    processed = performMarkFavorite(contacts, false);
                    message = "Unmarked " + processed + " contacts as favorite";
                    break;
                case CHANGE_RELATIONSHIP:
                    processed = performChangeRelationship(contacts, (String) request.getActionData());
                    message = "Changed relationship for " + processed + " contacts";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported action type");
            }
        } catch (Exception e) {
            failed = contacts.size() - processed;
            message = "Operation completed with " + failed + " failures: " + e.getMessage();
        }

        return BulkActionResponse.builder()
            .totalRequested(contacts.size())
            .processed(processed)
            .failed(failed)
            .timestamp(LocalDateTime.now())
            .message(message)
            .build();
    }

    private int performDelete(List<Contact> contacts, User user, HttpServletRequest request) {
        for (Contact contact : contacts) {
            contactRepository.delete(contact);
            activityService.logActivity(contact, user, ActivityType.DELETED,
                "Bulk delete operation", request);
        }
        return contacts.size();
    }

    private int performAddTag(List<Contact> contacts, Long tagId) {
        ContactTag tag = tagRepository.findById(tagId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));

        for (Contact contact : contacts) {
            if (!contact.getTags().contains(tag)) {
                contact.getTags().add(tag);
            }
        }
        contactRepository.saveAll(contacts);
        return contacts.size();
    }

    private int performRemoveTag(List<Contact> contacts, Long tagId) {
        for (Contact contact : contacts) {
            contact.getTags().removeIf(tag -> tag.getId().equals(tagId));
        }
        contactRepository.saveAll(contacts);
        return contacts.size();
    }

    private int performMarkFavorite(List<Contact> contacts, boolean isFavorite) {
        contacts.forEach(contact -> contact.setFavorite(isFavorite));
        contactRepository.saveAll(contacts);
        return contacts.size();
    }

    private int performChangeRelationship(List<Contact> contacts, String relationship) {
        contacts.forEach(contact -> {
            try {
                contact.setRelationship(com.scm.contactmanager.entities.Relationship.valueOf(relationship));
            } catch (IllegalArgumentException e) {
                // Log error and skip
            }
        });
        contactRepository.saveAll(contacts);
        return contacts.size();
    }
}
