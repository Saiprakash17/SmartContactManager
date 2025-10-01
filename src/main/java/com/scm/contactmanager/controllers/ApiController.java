package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.ApiResponse;
import com.scm.contactmanager.services.ApiService;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/contact/{id}")
    public ResponseEntity<ApiResponse<Contact>> getContact(@PathVariable @NotBlank String id) {
        Contact contact = apiService.getContact(id);
        return ResponseEntity.ok(ApiResponse.success(contact));
    }
    
    @GetMapping("/contacts/search")
    public ResponseEntity<ApiResponse<List<Contact>>> searchContacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Contact> contacts = apiService.searchContacts(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(contacts));
    }
    
    @PutMapping("/contact/{id}/favorite")
    public ResponseEntity<ApiResponse<Contact>> toggleFavorite(@PathVariable @NotBlank String id) {
        Contact contact = apiService.toggleFavorite(id);
        return ResponseEntity.ok(ApiResponse.success(contact));
    }
}
