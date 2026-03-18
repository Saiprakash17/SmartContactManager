package com.scm.contactmanager.controllers;

import java.time.LocalDate;
import java.util.List;

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

import com.scm.contactmanager.dtos.ImportantDateResponse;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ImportantDate;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.ImportantDateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@RestController
@RequestMapping("/api/contacts/{contactId}/important-dates")
@Validated
@Tag(name = "Important Dates & Reminders", description = "API endpoints for managing important dates and birthday reminders")
@SecurityRequirement(name = "bearerAuth")
public class ImportantDateController {

    private final ImportantDateService importantDateService;
    private final ContactRepo contactRepository;

    public ImportantDateController(ImportantDateService importantDateService,
                                  ContactRepo contactRepository) {
        this.importantDateService = importantDateService;
        this.contactRepository = contactRepository;
    }

    @PostMapping
    @Operation(summary = "Create an important date", description = "Add a birthday, anniversary, or other important date to a contact")
    public ResponseEntity<ApiResponse<ImportantDateResponse>> createImportantDate(
            @PathVariable Long contactId,
            @Valid @RequestBody CreateImportantDateRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        ImportantDate date = importantDateService.createImportantDate(
            contact,
            request.getName(),
            request.getDate().toString(),
            request.getNotificationEnabled(),
            request.getDaysBeforeNotify()
        );
        
        ImportantDateResponse response = convertToResponse(date);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Important date created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all important dates", description = "Retrieve all important dates for a specific contact")
    public ResponseEntity<ApiResponse<List<ImportantDateResponse>>> getImportantDates(
            @PathVariable Long contactId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        List<ImportantDate> dates = importantDateService.getContactDates(contact);
        List<ImportantDateResponse> responses = dates.stream()
            .map(this::convertToResponse)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Important dates retrieved successfully", responses));
    }

    @GetMapping("/{dateId}")
    @Operation(summary = "Get important date by ID", description = "Retrieve a specific important date by its ID")
    public ResponseEntity<ApiResponse<ImportantDateResponse>> getImportantDate(
            @PathVariable Long contactId,
            @PathVariable Long dateId,
            Authentication authentication) {
        
        ImportantDate date = importantDateService.getImportantDateById(dateId);
        
        // Verify contact ownership
        User user = (User) authentication.getPrincipal();
        if (date.getContact().getId() != contactId || !date.getContact().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "Forbidden"));
        }
        
        ImportantDateResponse response = convertToResponse(date);
        
        return ResponseEntity.ok(ApiResponse.success("Important date retrieved successfully", response));
    }

    @PutMapping("/{dateId}")
    @Operation(summary = "Update important date", description = "Update an existing important date")
    public ResponseEntity<ApiResponse<ImportantDateResponse>> updateImportantDate(
            @PathVariable Long contactId,
            @PathVariable Long dateId,
            @Valid @RequestBody CreateImportantDateRequest request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        ImportantDate existingDate = importantDateService.getImportantDateById(dateId);
        
        // Verify ownership
        if (existingDate.getContact().getId() != contactId || !existingDate.getContact().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "Forbidden"));
        }
        
        importantDateService.updateImportantDate(
            dateId,
            request.getName(),
            request.getDate().toString(),
            request.getNotificationEnabled(),
            request.getDaysBeforeNotify()
        );
        
        ImportantDate updatedDate = importantDateService.getImportantDateById(dateId);
        ImportantDateResponse response = convertToResponse(updatedDate);
        
        return ResponseEntity.ok(ApiResponse.success("Important date updated successfully", response));
    }

    @DeleteMapping("/{dateId}")
    @Operation(summary = "Delete important date", description = "Delete an important date entry")
    public ResponseEntity<ApiResponse<Void>> deleteImportantDate(
            @PathVariable Long contactId,
            @PathVariable Long dateId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        ImportantDate date = importantDateService.getImportantDateById(dateId);
        
        // Verify ownership
        if (date.getContact().getId() != contactId || !date.getContact().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "Forbidden"));
        }
        
        importantDateService.deleteImportantDate(dateId);
        
        return ResponseEntity.ok(ApiResponse.success("Important date deleted successfully", null));
    }

    @PutMapping("/{dateId}/toggle-notification")
    @Operation(summary = "Toggle notification", description = "Enable or disable notifications for an important date")
    public ResponseEntity<ApiResponse<ImportantDateResponse>> toggleNotification(
            @PathVariable Long contactId,
            @PathVariable Long dateId,
            @RequestParam Boolean enabled,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        ImportantDate date = importantDateService.getImportantDateById(dateId);
        
        // Verify ownership
        if (date.getContact().getId() != contactId || !date.getContact().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "Forbidden"));
        }
        
        date.setNotificationEnabled(enabled);
        // Need to save the changes
        importantDateService.updateImportantDate(
            dateId,
            date.getName(),
            date.getDate().toString(),
            enabled,
            date.getDaysBeforeNotify()
        );
        
        ImportantDate updatedDate = importantDateService.getImportantDateById(dateId);
        ImportantDateResponse response = convertToResponse(updatedDate);
        
        return ResponseEntity.ok(ApiResponse.success("Notification status updated successfully", response));
    }

    private ImportantDateResponse convertToResponse(ImportantDate date) {
        return ImportantDateResponse.builder()
            .id(date.getId())
            .contactId(date.getContact().getId())
            .contactName(date.getContact().getName())
            .name(date.getName())
            .date(date.getDate())
            .notificationEnabled(date.getNotificationEnabled())
            .daysBeforeNotify(date.getDaysBeforeNotify())
            .lastNotified(date.getLastNotified())
            .createdAt(date.getCreatedAt())
            .build();
    }

    // Request DTO
    @Data
    public static class CreateImportantDateRequest {
        @NotBlank(message = "Date name is required")
        private String name;

        @NotNull(message = "Date is required")
        private LocalDate date;

        private Boolean notificationEnabled;
        private Integer daysBeforeNotify;
    }
}
