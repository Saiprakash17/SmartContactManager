package com.scm.contactmanager.dtos;

import java.time.LocalDate;
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
public class ImportantDateResponse {
    private Long id;
    private Long contactId;
    private String contactName;
    private String name;
    private LocalDate date;
    private Boolean notificationEnabled;
    private Integer daysBeforeNotify;
    private LocalDateTime lastNotified;
    private LocalDateTime createdAt;
}
