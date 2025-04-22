package com.scm.contactmanager.forms;

import org.springframework.web.multipart.MultipartFile;

import com.scm.contactmanager.validators.ValidFile;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProfileForm {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Current Password is required")
    @Size(min = 6, max = 50, message = "Current Password must be between 6 and 50 characters")
    private String currentPassword;

    @NotBlank(message = "Phone Number is required")
    private String phoneNumber;

    @NotBlank(message = "About is required")
    private String about;

    @ValidFile(message = "Invalid file", checkEmpty = false)
    private MultipartFile contactImage;

    // This field will be populated after image upload
    private String picture;
}
