package com.scm.contactmanager.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateCommunicationLogRequest {

    @NotNull(message = "Communication type is required")
    private String type; // EMAIL, CALL, MEETING, SMS, MESSAGE, VISIT, OTHER

    @NotBlank(message = "Notes are required")
    @Size(min = 1, max = 5000, message = "Notes must be between 1 and 5000 characters")
    private String notes;

    @Size(max = 500, message = "Outcome must not exceed 500 characters")
    private String outcome;

    private LocalDateTime nextFollowUp;
}
