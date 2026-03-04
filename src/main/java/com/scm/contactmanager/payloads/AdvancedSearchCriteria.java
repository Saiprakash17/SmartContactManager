package com.scm.contactmanager.payloads;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdvancedSearchCriteria {
    private String name;
    private String email;
    private String phoneNumber;
    private String relationship;
    private String city;
    private String state;
    private String country;
    private LocalDate birthdateFrom;
    private LocalDate birthdateTo;
    
    @JsonProperty("isFavorite")
    private Boolean isFavorite;
    
    private List<Long> tagIds;
    private String websiteUrl;
    private LocalDate lastContactedFrom;
    private LocalDate lastContactedTo;
    @Builder.Default
    private String sortBy = "name"; // name, createdAt, lastModified
    @Builder.Default
    private String sortDirection = "ASC"; // ASC, DESC
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}
