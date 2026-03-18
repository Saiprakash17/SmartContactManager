package com.scm.contactmanager.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.scm.contactmanager.dtos.CommunicationLogResponse;
import com.scm.contactmanager.dtos.CreateCommunicationLogRequest;
import com.scm.contactmanager.entities.CommunicationLog;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.CommunicationLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contacts/{contactId}/communications")
@Validated
@Tag(name = "Communication Logs", description = "API endpoints for managing communication logs with contacts")
@SecurityRequirement(name = "bearerAuth")
public class CommunicationLogController {

    private final CommunicationLogService communicationLogService;
    private final ContactRepo contactRepository;

    public CommunicationLogController(CommunicationLogService communicationLogService,
                                     ContactRepo contactRepository) {
        this.communicationLogService = communicationLogService;
        this.contactRepository = contactRepository;
    }

    @PostMapping
    @Operation(summary = "Log a communication", description = "Create a new communication log entry for a contact")
    public ResponseEntity<ApiResponse<CommunicationLogResponse>> createCommunicationLog(
            @PathVariable Long contactId,
            @Valid @RequestBody CreateCommunicationLogRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        CommunicationLog log = communicationLogService.createCommunicationLog(contactId, request, user);
        
        CommunicationLogResponse response = CommunicationLogResponse.builder()
            .id(log.getId())
            .contactId(log.getContact().getId())
            .contactName(log.getContact().getName())
            .type(log.getType().toString())
            .notes(log.getNotes())
            .outcome(log.getOutcome())
            .timestamp(log.getTimestamp())
            .nextFollowUp(log.getNextFollowUp())
            .lastModified(log.getLastModified())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Communication logged successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all communications", description = "Retrieve all communication logs for a specific contact")
    public ResponseEntity<ApiResponse<Page<CommunicationLogResponse>>> getCommunications(
            @PathVariable Long contactId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<CommunicationLogResponse> communications = communicationLogService.getContactCommunications(contact, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Communications retrieved successfully", communications));
    }

    @GetMapping("/{logId}")
    @Operation(summary = "Get communication by ID", description = "Retrieve a specific communication log by its ID")
    public ResponseEntity<ApiResponse<CommunicationLog>> getCommunicationLog(
            @PathVariable Long contactId,
            @PathVariable Long logId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        CommunicationLog log = communicationLogService.getCommunicationLogById(logId);
        
        // Verify contact ownership
        if (log.getContact().getId() != contactId || !log.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "Forbidden"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Communication retrieved successfully", log));
    }

    @PutMapping("/{logId}")
    @Operation(summary = "Update communication", description = "Update an existing communication log")
    public ResponseEntity<ApiResponse<CommunicationLogResponse>> updateCommunicationLog(
            @PathVariable Long contactId,
            @PathVariable Long logId,
            @Valid @RequestBody CreateCommunicationLogRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        CommunicationLog updatedLog = communicationLogService.updateCommunicationLog(logId, request, user);
        
        CommunicationLogResponse response = CommunicationLogResponse.builder()
            .id(updatedLog.getId())
            .contactId(updatedLog.getContact().getId())
            .contactName(updatedLog.getContact().getName())
            .type(updatedLog.getType().toString())
            .notes(updatedLog.getNotes())
            .outcome(updatedLog.getOutcome())
            .timestamp(updatedLog.getTimestamp())
            .nextFollowUp(updatedLog.getNextFollowUp())
            .lastModified(updatedLog.getLastModified())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success("Communication updated successfully", response));
    }

    @DeleteMapping("/{logId}")
    @Operation(summary = "Delete communication", description = "Delete a communication log entry")
    public ResponseEntity<ApiResponse<Void>> deleteCommunicationLog(
            @PathVariable Long contactId,
            @PathVariable Long logId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        communicationLogService.deleteCommunicationLog(logId, user);
        
        return ResponseEntity.ok(ApiResponse.success("Communication deleted successfully", null));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent communications", description = "Get recent communications for a contact (last N days)")
    public ResponseEntity<ApiResponse<List<CommunicationLogResponse>>> getRecentCommunications(
            @PathVariable Long contactId,
            @RequestParam(defaultValue = "30") int days,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        List<CommunicationLogResponse> communications = communicationLogService.getRecentCommunications(contact, days);
        
        return ResponseEntity.ok(ApiResponse.success("Recent communications retrieved successfully", communications));
    }

    @GetMapping("/pending-followups")
    @Operation(summary = "Get pending follow-ups", description = "Get all communications that need follow-up for the authenticated user")
    public ResponseEntity<ApiResponse<List<CommunicationLogResponse>>> getPendingFollowups(
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        List<CommunicationLogResponse> pendingFollowups = communicationLogService.getPendingFollowups(user);
        
        return ResponseEntity.ok(ApiResponse.success("Pending follow-ups retrieved successfully", pendingFollowups));
    }

    @GetMapping("/count")
    @Operation(summary = "Get communication count", description = "Get the total number of communications for a contact")
    public ResponseEntity<ApiResponse<Long>> getCommunicationCount(
            @PathVariable Long contactId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        Long count = communicationLogService.getCommunicationCount(contact);
        
        return ResponseEntity.ok(ApiResponse.success("Communication count retrieved successfully", count));
    }
}
