package com.scm.contactmanager.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.ResourseNotFoundException;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.ContactService;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepo contactRepo;

    @Override
    public Contact saveContact(Contact contact) {
        contactRepo.save(contact);
        return contact;
    }

    @Override
    public Contact getContactById(Long id) {
        return contactRepo.findById(id).orElseThrow(() -> new ResourseNotFoundException("Contact not found with id: " + id));
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactRepo.findAll();
    }

    @Override
    public void deleteContact(Long id) {
        Contact contact = contactRepo.findById(id)
                .orElseThrow(() -> new ResourseNotFoundException("Contact not found with id: " + id));
        contactRepo.delete(contact);
    }

    @Override
    public Contact updateContact(Contact contact) {
        Contact existingContact = contactRepo.findById(contact.getId())
                .orElseThrow(() -> new ResourseNotFoundException("Contact not found with id: " + contact.getId()));
        existingContact.setName(contact.getName());
        existingContact.setEmail(contact.getEmail());
        existingContact.setPhoneNumber(contact.getPhoneNumber());
        existingContact.setImageUrl(contact.getImageUrl());
        existingContact.setAbout(contact.getAbout());
        existingContact.setAddress(contact.getAddress());
        existingContact.setFavorite(contact.isFavorite());
        return contactRepo.save(existingContact);
    }

    @Override
    public List<Contact> getAllContactsByUserId(String userId) {
        return contactRepo.findByUserId(userId);
    }

    // @Override
    // public List<Contact> getAllFavoriteContactsByUserId(String userId) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getAllFavoriteContactsByUserId'");
    // }

    @Override
    public List<Contact> getByUser(User user) {
        return contactRepo.findByUser(user);
    }

}
