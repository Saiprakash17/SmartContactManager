package com.scm.contactmanager.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scm.contactmanager.dtos.ActivityStatistics;
import com.scm.contactmanager.dtos.ActivityTimelineResponse;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactActivity;
import com.scm.contactmanager.entities.ContactActivity.ActivityType;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.payloads.ApiResponse;
import com.scm.contactmanager.repositories.ContactActivityRepo;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.ContactActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/contacts/{contactId}/timeline")
@Validated
@Tag(name = "Activity Timeline", description = "API endpoints for viewing activity timeline and audit logs for contacts")
@SecurityRequirement(name = "bearerAuth")
public class ActivityTimelineController {

    private final ContactActivityService contactActivityService;
    private final ContactRepo contactRepository;
    private final ContactActivityRepo contactActivityRepository;

    public ActivityTimelineController(ContactActivityService contactActivityService,
                                     ContactRepo contactRepository,
                                     ContactActivityRepo contactActivityRepository) {
        this.contactActivityService = contactActivityService;
        this.contactRepository = contactRepository;
        this.contactActivityRepository = contactActivityRepository;
    }

    @GetMapping
    @Operation(summary = "Get contact activity timeline", description = "Retrieve the complete activity timeline for a specific contact")
    public ResponseEntity<ApiResponse<List<ActivityTimelineResponse>>> getContactTimeline(
            @PathVariable Long contactId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        List<ContactActivity> activities = contactActivityService.getContactTimeline(contact);
        List<ActivityTimelineResponse> responses = activities.stream()
            .map(this::convertToResponse)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Activity timeline retrieved successfully", responses));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent activities", description = "Get recent activities for the authenticated user")
    public ResponseEntity<ApiResponse<List<ActivityTimelineResponse>>> getRecentActivities(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        List<ContactActivity> activities = contactActivityService.getRecentActivities(user, days);
        
        List<ActivityTimelineResponse> responses = activities.stream()
            .limit(limit)
            .map(this::convertToResponse)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Recent activities retrieved successfully", responses));
    }

    @GetMapping("/by-type/{activityType}")
    @Operation(summary = "Get activities by type", description = "Get activities of a specific type for a contact")
    public ResponseEntity<ApiResponse<List<ActivityTimelineResponse>>> getActivitiesByType(
            @PathVariable Long contactId,
            @PathVariable String activityType,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        ActivityType type = ActivityType.valueOf(activityType.toUpperCase());
        List<ContactActivity> activities = contactActivityRepository.findByContactAndActivityTypeOrderByTimestampDesc(contact, type);
        
        List<ActivityTimelineResponse> responses = activities.stream()
            .map(this::convertToResponse)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Activities retrieved successfully", responses));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get activity statistics", description = "Get activity statistics for a contact")
    public ResponseEntity<ApiResponse<ActivityStatistics>> getActivityStats(
            @PathVariable Long contactId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new RuntimeException("Contact not found"));
        
        List<ContactActivity> activities = contactActivityService.getContactTimeline(contact);
        
        ActivityStatistics stats = ActivityStatistics.builder()
            .totalActivities((long) activities.size())
            .created(countByType(activities, ActivityType.CREATED))
            .viewed(countByType(activities, ActivityType.VIEWED))
            .updated(countByType(activities, ActivityType.UPDATED))
            .deleted(countByType(activities, ActivityType.DELETED))
            .exported(countByType(activities, ActivityType.EXPORTED))
            .shared(countByType(activities, ActivityType.SHARED))
            .tagged(countByType(activities, ActivityType.TAGGED))
            .favorited(countByType(activities, ActivityType.FAVORITED))
            .communicationLogged(countByType(activities, ActivityType.COMMUNICATION_LOGGED))
            .lastActivity(activities.isEmpty() ? null : activities.get(0).getTimestamp())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success("Activity statistics retrieved successfully", stats));
    }

    @GetMapping("/{activityId}")
    @Operation(summary = "Get activity details", description = "Get detailed information about a specific activity")
    public ResponseEntity<ApiResponse<ActivityTimelineResponse>> getActivityDetails(
            @PathVariable Long contactId,
            @PathVariable Long activityId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        ContactActivity activity = contactActivityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        // Verify ownership
        if (activity.getContact().getId() != contactId || !activity.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", "Forbidden"));
        }
        
        ActivityTimelineResponse response = convertToResponse(activity);
        
        return ResponseEntity.ok(ApiResponse.success("Activity retrieved successfully", response));
    }

    private ActivityTimelineResponse convertToResponse(ContactActivity activity) {
        return ActivityTimelineResponse.builder()
            .id(activity.getId())
            .contactId(activity.getContact().getId())
            .contactName(activity.getContact().getName())
            .activityType(activity.getActivityType().toString())
            .description(activity.getDescription())
            .timestamp(activity.getTimestamp())
            .ipAddress(activity.getIpAddress())
            .userAgent(activity.getUserAgent())
            .build();
    }

    private long countByType(List<ContactActivity> activities, ActivityType type) {
        return activities.stream()
            .filter(a -> a.getActivityType() == type)
            .count();
    }
}
