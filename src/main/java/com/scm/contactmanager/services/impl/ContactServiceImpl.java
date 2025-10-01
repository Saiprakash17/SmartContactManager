package com.scm.contactmanager.services.impl;
import com.scm.contactmanager.services.UserService;
import org.springframework.web.multipart.MultipartFile;
import com.scm.contactmanager.entities.Address;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.scm.contactmanager.forms.ContactForm;
import com.scm.contactmanager.forms.ContactsSearchForm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.ContactService;

@Service
public class ContactServiceImpl implements ContactService {
    @Autowired
    private UserService userService;
    @Autowired
    private QRCodeGeneratorService qrCodeGeneratorService;
    @Autowired
    private ImageService imageService;

    @Override
    public Contact saveContact(ContactForm contactForm, org.springframework.security.core.Authentication authentication) {
        String username = com.scm.contactmanager.helper.UserHelper.getEmailOfLoggedInUser(authentication);
        User user = userService.getUserByEmail(username);
        Contact contact = new Contact();
        populateContactFromForm(contact, contactForm);
        contact.setUser(user);
        
        // Process image if provided
        if (contactForm.getContactImage() != null && !contactForm.getContactImage().isEmpty()) {
            processContactImage(contact, contactForm.getContactImage());
        }

        return saveContact(contact);
    }

    @Override
    public Page<Contact> getContactsForUser(User user, int page, int size, String sortBy, String direction) {
        return getByUser(user, page, size, sortBy, direction);
    }

    @Override
    public Page<Contact> searchContacts(User user, ContactsSearchForm contactSearchForm, int page, int size, String sortBy, String direction) {
        String field = contactSearchForm.getField();
        String keyword = contactSearchForm.getKeyword();

        if (field == null || field.isEmpty() || keyword == null || keyword.isEmpty()) {
            return getByUser(user, page, size, sortBy, direction);
        }

        switch (field) {
            case "name":
                return searchByName(keyword, size, page, sortBy, direction, user);
            case "email":
                return searchByEmail(keyword, size, page, sortBy, direction, user);
            case "phoneNumber":
                return searchByPhoneNumber(keyword, size, page, sortBy, direction, user);
            case "relationship":
                return searchByRelationship(keyword, size, page, sortBy, direction, user);
            default:
                return getByUser(user, page, size, sortBy, direction);
        }
    }

    @Override
    public Contact editContact(Long contactId) {
        return getContactById(contactId);
    }

    @Override
    public org.springframework.http.ResponseEntity<byte[]> getContactQRCode(Long contactId, int width, int height) {
        try {
            // Validate dimensions
            if (width <= 0 || height <= 0 || width > 1000 || height > 1000) {
                return org.springframework.http.ResponseEntity.badRequest().build();
            }

            // Get contact and validate it exists
            Contact contact = getContactById(contactId);
            if (contact == null) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            // Generate QR code
            byte[] qrCode = qrCodeGeneratorService.generateQRCodeFromContact(contact, width, height);
            if (qrCode == null || qrCode.length == 0) {
                return org.springframework.http.ResponseEntity.badRequest().build();
            }

            // Return successful response
            return org.springframework.http.ResponseEntity
                .ok()
                .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                .body(qrCode);

        } catch (ResourceNotFoundException e) {
            return org.springframework.http.ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().build();
        }
    }

    @Override
    public org.springframework.http.ResponseEntity<String> decodeContactQR(org.springframework.web.multipart.MultipartFile file) {
        try {
            String decodedJson = qrCodeGeneratorService.decodeContactQR(file);
            return org.springframework.http.ResponseEntity.ok(decodedJson);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("{\"error\":\"Could not decode QR code. " + e.getMessage().replaceAll("\"", "'") + "\"}");
        }
    }

    @Autowired
    private ContactRepo contactRepo;

    

    @Override
    public Contact saveContact(Contact contact) {
        contactRepo.save(contact);
        return contact;
    }

    @Override
    public Contact getContactById(Long id) {
        return contactRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactRepo.findAll();
    }

    @Override
    public void deleteContact(Long id) {
        Contact contact = contactRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        contactRepo.delete(contact);
    }

    @Override
    public Contact updateContact(Contact contact) {
        Contact existingContact = contactRepo.findById(contact.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contact.getId()));
        existingContact.setName(contact.getName());
        existingContact.setEmail(contact.getEmail());
        existingContact.setPhoneNumber(contact.getPhoneNumber());
        existingContact.setImageUrl(contact.getImageUrl());
        existingContact.setAbout(contact.getAbout());
        existingContact.setAddress(contact.getAddress());
        existingContact.setFavorite(contact.isFavorite());
        existingContact.setLinkedin(contact.getLinkedin());
        existingContact.setWebsite(contact.getWebsite());
        existingContact.setCloudinaryImagePublicId(contact.getCloudinaryImagePublicId());
        existingContact.setUser(contact.getUser());
        return contactRepo.save(existingContact);
    }

    private void processContactImage(Contact contact, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            String fileName = java.util.UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(image, fileName);
            contact.setImageUrl(fileURL);
            contact.setCloudinaryImagePublicId(fileName);
        }
    }

    private void populateContactFromForm(Contact contact, ContactForm form) {
        // Create and set address
        Address address = new Address();
        address.setStreet(form.getStreet());
        address.setCity(form.getCity());
        address.setState(form.getState());
        address.setZipCode(form.getZipCode());
        address.setCountry(form.getCountry());

        // Set contact fields
        contact.setName(form.getName());
        contact.setEmail(form.getEmail());
        contact.setPhoneNumber(form.getPhoneNumber());
        contact.setFavorite(form.isFavorite());
        contact.setAddress(address);
        contact.setAbout(form.getDescription());
        contact.setLinkedin(form.getLinkedInLink());
        contact.setWebsite(form.getWebsiteLink());
        contact.setRelationship(form.getRelationship());
    }
    

    @Override
    public List<Contact> getAllContactsByUserId(String userId) {
        return contactRepo.findByUserId(userId);
    }


    @Override
    public Page<Contact> getByUser(User user, int pageNumber, int pageSize, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
        return contactRepo.findByUser(user, pageable);
    }

    @Override
    public Page<Contact> searchByName(String nameKeyword, int size, int page, String sortBy, String order, User user) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        if (nameKeyword == null || nameKeyword.trim().isEmpty()) {
            return contactRepo.findByUser(user, pageable);
        }
        return contactRepo.findByUserAndNameContaining(user, nameKeyword, pageable);
    }

    @Override
    public Page<Contact> searchByEmail(String emailKeyword, int size, int page, String sortBy, String order, User user) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndEmailContaining(user, emailKeyword, pageable);
    }

    @Override
    public Page<Contact> searchByPhoneNumber(String phoneNumberKeyword, int size, int page, String sortBy, String order, User user) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        var pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndPhoneNumberContaining(user, phoneNumberKeyword, pageable);
    }

    @Override
    public Page<Contact> searchByRelationship(String relationshipKeyword, int size, int page, String sortBy, String order, User user) {
        Sort sort = order.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndRelationshipContainingIgnoreCase(user, relationshipKeyword, pageable);
    }

    

    @Override
    public Page<Contact> searchFavoriteByName(String keyword, int size, int page, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndFavoriteTrueAndNameContaining(user, keyword, pageable);
    }

    @Override
    public Page<Contact> searchFavoriteByEmail(String keyword, int size, int page, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndFavoriteTrueAndEmailContaining(user, keyword, pageable);
    }

    @Override
    public Page<Contact> searchFavoriteByPhoneNumber(String keyword, int size, int page, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndFavoriteTrueAndPhoneNumberContaining(user, keyword, pageable);
    }

    @Override
    public Page<Contact> searchFavoriteByRelationship(String keyword, int size, int page, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndFavoriteTrueAndRelationshipContainingIgnoreCase(user, keyword, pageable);
    }

    @Override
    public Page<Contact> getFavoriteContactsByUser(User user, int pageNumber, int pageSize, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
        return contactRepo.findByUserAndFavoriteTrue(user, pageable);
    }

    @Override
    public List<Contact> searchContacts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<Contact> results;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            results = contactRepo.findAll(pageable);
        } else {
            results = contactRepo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
                keyword, keyword, keyword, pageable);
        }
        
        return results.getContent();
    }
    
    @Override
    public Contact toggleFavorite(Long contactId) {
        Contact contact = getContactById(contactId);
        contact.setFavorite(!contact.isFavorite());
        return contactRepo.save(contact);
    }
}
