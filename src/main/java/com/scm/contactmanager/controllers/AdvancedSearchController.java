package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.AdvancedSearchCriteria;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.services.AdvancedSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/search")
@Validated
@Tag(name = "Advanced Search", description = "Advanced search and filtering for contacts")
@SecurityRequirement(name = "bearerAuth")
public class AdvancedSearchController {

    @Autowired
    private AdvancedSearchService advancedSearchService;

    @PostMapping("/advanced")
    @Operation(summary = "Advanced search", description = "Search contacts with multiple criteria filters")
    public ResponseEntity<ApiResponse<Page<Contact>>> advancedSearch(
            @Valid @RequestBody AdvancedSearchCriteria criteria,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Page<Contact> results = advancedSearchService.search(criteria, user);
        
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", results));
    }
}
