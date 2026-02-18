package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.payloads.BulkActionRequest;
import com.scm.contactmanager.payloads.BulkActionResponse;
import com.scm.contactmanager.services.BulkActionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bulk")
@Validated
@Tag(name = "Bulk Operations", description = "Bulk operations on multiple contacts")
@SecurityRequirement(name = "bearerAuth")
public class BulkActionController {

    @Autowired
    private BulkActionService bulkActionService;

    @PostMapping("/actions")
    @Operation(summary = "Perform bulk action", description = "Perform bulk operations on multiple contacts (delete, tag, favorite, etc.)")
    public ResponseEntity<ApiResponse<BulkActionResponse>> performBulkAction(
            @Valid @RequestBody BulkActionRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User user = (User) authentication.getPrincipal();
        BulkActionResponse response = bulkActionService.performBulkAction(request, user, httpRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Bulk action completed", response));
    }
}
