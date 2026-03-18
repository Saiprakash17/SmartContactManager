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
public class CommunicationLogResponse {

    private Long id;
    private Long contactId;
    private String contactName;
    private String type;
    private String notes;
    private String outcome;
    private LocalDateTime timestamp;
    private LocalDateTime nextFollowUp;
    private LocalDateTime lastModified;
}
