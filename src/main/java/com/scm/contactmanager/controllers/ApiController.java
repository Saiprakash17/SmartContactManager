package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.services.ApiService;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Contact Management API", description = "API endpoints for managing contacts")
@SecurityRequirement(name = "bearerAuth")
public class ApiController {

    @Autowired
    private ApiService apiService;

    @GetMapping("/contact/{id}")
    @Operation(summary = "Get contact by ID", description = "Retrieve a single contact by its unique identifier")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contact retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contact not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid contact ID")
    })
    public ResponseEntity<ApiResponse<Contact>> getContact(@PathVariable(required = false) String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact ID cannot be null or empty");
        }
        Contact contact = apiService.getContact(id);
        return ResponseEntity.ok(ApiResponse.success("Contact retrieved successfully", contact));
    }
    
    @GetMapping("/contacts/search")
    @Operation(summary = "Search contacts", description = "Search contacts by keyword with pagination")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contacts retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<ApiResponse<List<Contact>>> searchContacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Contact> contacts = apiService.searchContacts(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Contacts retrieved successfully", contacts));
    }
    
    @PutMapping("/contact/{id}/favorite")
    @Operation(summary = "Toggle contact favorite status", description = "Mark or unmark a contact as favorite")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Favorite status updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Contact not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid contact ID")
    })
    public ResponseEntity<ApiResponse<Contact>> toggleFavorite(@PathVariable @NotBlank String id) {
        Contact contact = apiService.toggleFavorite(id);
        return ResponseEntity.ok(ApiResponse.success("Favorite status updated successfully", contact));
    }
}
