package com.scm.contactmanager.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;

public interface ContactService {

    //save contact
    Contact saveContact(Contact contact);

    //get contact by id
    Contact getContactById(Long id);

    //get all contacts
    List<Contact> getAllContacts();

    //delete contact
    void deleteContact(Long id);

    //update contact
    Contact updateContact(Contact contact);

    //get all contacts by user id
    List<Contact> getAllContactsByUserId(String userId);

    //get all favorite contacts by user id
    // List<Contact> getAllFavoriteContactsByUserId(String userId);

    Page<Contact> getByUser(User user, int pageNumber, int pageSize, String sortBy, String sortDir);

    Page<Contact> searchByName(String keyword, int size, int page, String sortBy, String direction, User user);

    Page<Contact> searchByEmail(String keyword, int size, int page, String sortBy, String direction, User user);

    Page<Contact> searchByPhoneNumber(String keyword, int size, int page, String sortBy, String direction, User user);
}
