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
public class ActivityStatistics {
    private long totalActivities;
    private long created;
    private long viewed;
    private long updated;
    private long deleted;
    private long exported;
    private long shared;
    private long tagged;
    private long favorited;
    private long communicationLogged;
    private LocalDateTime lastActivity;
}
