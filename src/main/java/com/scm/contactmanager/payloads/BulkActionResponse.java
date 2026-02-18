package com.scm.contactmanager.payloads;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkActionResponse {

    @JsonProperty("totalRequested")
    private Integer totalRequested;

    @JsonProperty("processed")
    private Integer processed;

    @JsonProperty("failed")
    private Integer failed;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("message")
    private String message;
}
