package com.scm.contactmanager.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import java.util.List;

@Service
public class ApiServiceImpl implements ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiServiceImpl.class);
    
    @Autowired
    private ContactService contactService;
    
    @Override
    public Contact getContact(String id) {
        logger.info("Fetching contact with id: {}", id);
        Long contactId = validateAndParseContactId(id);
        Contact contact = contactService.getContactById(contactId);
        
        if (contact == null) {
            throw new ResourceNotFoundException("Contact", "id", id);
        }
        
        return contact;
    }
    
    @Override
    public List<Contact> searchContacts(String keyword, int page, int size) {
        logger.info("Searching contacts with keyword: {}, page: {}, size: {}", keyword, page, size);
        return contactService.searchContacts(keyword, page, size);
    }
    
    @Override
    public Contact toggleFavorite(String id) {
        logger.info("Toggling favorite status for contact with id: {}", id);
        Long contactId = validateAndParseContactId(id);
        Contact contact = contactService.getContactById(contactId);
        
        if (contact == null) {
            throw new ResourceNotFoundException("Contact", "id", id);
        }
        
        contact.setFavorite(!contact.isFavorite());
        return contactService.updateContact(contact);
    }
    
    @Override
    public Long validateAndParseContactId(String id) {
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Contact ID must be a valid number");
        }
    }
}