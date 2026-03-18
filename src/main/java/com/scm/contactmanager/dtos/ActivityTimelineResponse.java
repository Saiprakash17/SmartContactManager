package com.scm.contactmanager.dtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityTimelineResponse {
    private Long id;
    private Long contactId;
    private String contactName;
    private String activityType;
    private String description;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
}
