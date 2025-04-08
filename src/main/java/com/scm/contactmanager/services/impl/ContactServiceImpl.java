package com.scm.contactmanager.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.method.P;
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
    public Page<Contact> getByUser(User user, int pageNumber, int pageSize, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
        return contactRepo.findByUser(user, pageable);
    }

    @Override
    public Page<Contact> searchByName(String nameKeyword, int size, int page, String sortBy, String order, User user) {

        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndNameContaining(user, nameKeyword, pageable);
    }

    @Override
    public Page<Contact> searchByEmail(String emailKeyword, int size, int page, String sortBy, String order,
            User user) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndEmailContaining(user, emailKeyword, pageable);
    }

    @Override
    public Page<Contact> searchByPhoneNumber(String phoneNumberKeyword, int size, int page, String sortBy,
            String order, User user) {

        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndPhoneNumberContaining(user, phoneNumberKeyword, pageable);
    }
}
