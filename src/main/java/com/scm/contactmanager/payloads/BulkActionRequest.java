package com.scm.contactmanager.payloads;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkActionRequest {

    @NotNull(message = "Contact IDs cannot be null")
    @NotEmpty(message = "At least one contact ID is required")
    @JsonProperty("contactIds")
    private List<Long> contactIds;

    @NotNull(message = "Action type is required")
    @JsonProperty("actionType")
    private BulkActionType actionType;

    @JsonProperty("actionData")
    private Object actionData; // Generic field for action-specific data
}

