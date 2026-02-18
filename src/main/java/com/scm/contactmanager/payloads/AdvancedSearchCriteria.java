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
    private String sortBy = "name"; // name, createdAt, lastModified
    private String sortDirection = "ASC"; // ASC, DESC
    private Integer page = 0;
    private Integer size = 10;
}
