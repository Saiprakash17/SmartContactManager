package com.scm.contactmanager.services;

import java.util.List;

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

    List<Contact> getByUser(User user);
}
