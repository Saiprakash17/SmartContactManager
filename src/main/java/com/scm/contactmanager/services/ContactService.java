package com.scm.contactmanager.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;


public interface ContactService {

    // Save contact
    Contact saveContact(Contact contact);

    // Get contact by id
    Contact getContactById(Long id);

    // Get all contacts
    List<Contact> getAllContacts();

    // Delete contact
    void deleteContact(Long id);

    // Update contact
    Contact updateContact(Contact contact);

    // Get all contacts by user id
    List<Contact> getAllContactsByUserId(String userId);

    // Pageable queries
    Page<Contact> getByUser(User user, int pageNumber, int pageSize, String sortBy, String sortDir);

    Page<Contact> searchByName(String keyword, int size, int page, String sortBy, String direction, User user);

    Page<Contact> searchByEmail(String keyword, int size, int page, String sortBy, String direction, User user);

    Page<Contact> searchByPhoneNumber(String keyword, int size, int page, String sortBy, String direction, User user);

    Page<Contact> searchByRelationship(String keyword, int size, int page, String sortBy, String direction, User user);


    // Search favorite contacts by name
    Page<Contact> searchFavoriteByName(String keyword, int size, int page, String sortBy, String direction, User user);

    // Search favorite contacts by email
    Page<Contact> searchFavoriteByEmail(String keyword, int size, int page, String sortBy, String direction, User user);

    // Search favorite contacts by phone number
    Page<Contact> searchFavoriteByPhoneNumber(String keyword, int size, int page, String sortBy, String direction, User user);

    // Search favorite contacts by relationship
    Page<Contact> searchFavoriteByRelationship(String keyword, int size, int page, String sortBy, String direction, User user);

    // Get favorite contacts for a user (paginated)
    Page<Contact> getFavoriteContactsByUser(User user, int pageNumber, int pageSize, String sortBy, String sortDir);

}
