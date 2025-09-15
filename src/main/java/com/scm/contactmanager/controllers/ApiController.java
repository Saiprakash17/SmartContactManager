package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.ApiResponse;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.services.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ContactService contactService;

    @GetMapping("/contact/{id}")
    public ResponseEntity<ApiResponse<Contact>> getContact(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid input", "Contact ID cannot be null or empty"));
            }

            Long contactId;
            try {
                contactId = Long.parseLong(id);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid format", "Contact ID must be a valid number"));
            }

            Contact contact = contactService.getContactById(contactId);
            if (contact == null) {
                throw new ResourceNotFoundException("Contact", "id", id);
            }
            
            return ResponseEntity.ok(ApiResponse.success(contact));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Not found", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving contact with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", "An error occurred while retrieving the contact"));
        }
    }
}
