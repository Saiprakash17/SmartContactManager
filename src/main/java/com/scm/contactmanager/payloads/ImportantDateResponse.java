package com.scm.contactmanager.payloads;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportantDateResponse {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("contactId")
    private Long contactId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("date")
    private String date; // ISO format: yyyy-MM-dd

    @JsonProperty("notificationEnabled")
    private Boolean notificationEnabled;

    @JsonProperty("daysBeforeNotify")
    private Integer daysBeforeNotify;

    @JsonProperty("lastNotified")
    private String lastNotified; // ISO format with time

    @JsonProperty("createdAt")
    private String createdAt;
}
