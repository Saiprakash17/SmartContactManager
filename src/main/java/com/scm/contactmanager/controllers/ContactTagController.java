package com.scm.contactmanager.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactTag;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.services.ContactTagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/tags")
@Validated
@Tag(name = "Contact Tags Management", description = "API endpoints for managing contact tags")
@SecurityRequirement(name = "bearerAuth")
public class ContactTagController {

    @Autowired
    private ContactTagService tagService;

    @PostMapping
    @Operation(summary = "Create a new tag", description = "Create a new contact tag")
    public ResponseEntity<ApiResponse<ContactTag>> createTag(
            @Valid @RequestBody CreateTagRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        ContactTag tag = tagService.createTag(
            request.getName(),
            request.getColor(),
            request.getDescription(),
            user
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Tag created successfully", tag));
    }

    @GetMapping
    @Operation(summary = "Get all user tags", description = "Retrieve all tags for the authenticated user")
    public ResponseEntity<ApiResponse<List<ContactTag>>> getUserTags(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ContactTag> tags = tagService.getUserTags(user);
        
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tags));
    }

    @GetMapping("/{tagId}")
    @Operation(summary = "Get tag by ID", description = "Retrieve a specific tag by its ID")
    public ResponseEntity<ApiResponse<ContactTag>> getTagById(@PathVariable Long tagId) {
        ContactTag tag = tagService.getTagById(tagId);
        
        return ResponseEntity.ok(ApiResponse.success("Tag retrieved successfully", tag));
    }

    @PutMapping("/{tagId}")
    @Operation(summary = "Update tag", description = "Update an existing tag")
    public ResponseEntity<ApiResponse<Void>> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody CreateTagRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        tagService.updateTag(tagId, request.getName(), request.getColor(), request.getDescription(), user);
        
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully", null));
    }

    @DeleteMapping("/{tagId}")
    @Operation(summary = "Delete tag", description = "Delete a tag and remove it from all contacts")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Long tagId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        tagService.deleteTag(tagId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", null));
    }

    @PostMapping("/{tagId}/contacts/{contactId}")
    @Operation(summary = "Add tag to contact", description = "Add a tag to a specific contact")
    public ResponseEntity<ApiResponse<Void>> addTagToContact(
            @PathVariable Long tagId,
            @PathVariable Long contactId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        tagService.addTagToContact(contactId, tagId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Tag added to contact", null));
    }

    @DeleteMapping("/{tagId}/contacts/{contactId}")
    @Operation(summary = "Remove tag from contact", description = "Remove a tag from a specific contact")
    public ResponseEntity<ApiResponse<Void>> removeTagFromContact(
            @PathVariable Long tagId,
            @PathVariable Long contactId) {
        
        tagService.removeTagFromContact(contactId, tagId);
        
        return ResponseEntity.ok(ApiResponse.success("Tag removed from contact", null));
    }

    @GetMapping("/{tagId}/contacts")
    @Operation(summary = "Get contacts by tag", description = "Retrieve all contacts with a specific tag")
    public ResponseEntity<ApiResponse<Page<Contact>>> getContactsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Contact> contacts = tagService.getContactsByTag(tagId, page, size);
        
        return ResponseEntity.ok(ApiResponse.success("Contacts retrieved successfully", contacts));
    }
}

class CreateTagRequest {
    @NotBlank(message = "Tag name is required")
    private String name;
    private String color;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
