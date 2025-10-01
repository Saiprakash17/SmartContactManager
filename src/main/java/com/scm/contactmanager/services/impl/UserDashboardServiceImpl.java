package com.scm.contactmanager.services.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ChangePasswordForm;
import com.scm.contactmanager.forms.ProfileForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.UserDashboardService;
import com.scm.contactmanager.services.UserService;

@Service
public class UserDashboardServiceImpl implements UserDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(UserDashboardServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ImageService imageService;

    @Override
    public ProfileForm getProfileForm(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return new ProfileForm(
            user.getName(),
            user.getPassword(),
            user.getPhoneNumber(),
            user.getAbout(),
            null,
            user.getImageUrl()
        );
    }

    @Override
    public Message updateProfile(ProfileForm profileForm, User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            logger.error("Binding result has errors: {}", bindingResult.getAllErrors());
            return Message.builder()
                .content("Please correct the following errors")
                .type(MessageType.red)
                .build();
        }

        if (!userService.validatePassword(user, profileForm.getCurrentPassword())) {
            return Message.builder()
                .content("Current password is incorrect")
                .type(MessageType.red)
                .build();
        }

        try {
            user.setName(profileForm.getName());
            user.setPhoneNumber(profileForm.getPhoneNumber());
            user.setAbout(profileForm.getAbout());

            if (profileForm.getContactImage() != null && !profileForm.getContactImage().isEmpty()) {
                Message imageMessage = handleProfileImageUpload(user, profileForm.getContactImage());
                if (imageMessage.getType() == MessageType.red) {
                    return imageMessage;
                }
            }

            userService.updateUser(user);
            return Message.builder()
                .content("Profile updated successfully")
                .type(MessageType.green)
                .build();
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            return Message.builder()
                .content("Error updating profile: " + e.getMessage())
                .type(MessageType.red)
                .build();
        }
    }

    @Override
    public Message deleteAccount(User user) {
        try {
            userService.deleteUserById(user.getId());
            return Message.builder()
                .content("Your account has been deleted successfully")
                .type(MessageType.green)
                .build();
        } catch (Exception e) {
            logger.error("Error deleting account", e);
            return Message.builder()
                .content("Error deleting account: " + e.getMessage())
                .type(MessageType.red)
                .build();
        }
    }

    @Override
    public Message changePassword(ChangePasswordForm changePasswordForm, User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Message.builder()
                .content("Please correct the following errors")
                .type(MessageType.red)
                .build();
        }

        if (!userService.validatePassword(user, changePasswordForm.getCurrentPassword())) {
            return Message.builder()
                .content("Current password is incorrect")
                .type(MessageType.red)
                .build();
        }

        try {
            userService.updatePassword(user, changePasswordForm.getNewPassword());
            return Message.builder()
                .content("Password updated successfully")
                .type(MessageType.green)
                .build();
        } catch (Exception e) {
            logger.error("Error changing password", e);
            return Message.builder()
                .content("Error changing password: " + e.getMessage())
                .type(MessageType.red)
                .build();
        }
    }

    @Override
    public Message handleProfileImageUpload(User user, MultipartFile image) {
        try {
            String fileName = UUID.randomUUID().toString();
            String fileURL = imageService.uploadImage(image, fileName);
            user.setImageUrl(fileURL);
            user.setCloudinaryImagePublicId(fileName);
            return Message.builder()
                .content("Image uploaded successfully")
                .type(MessageType.green)
                .build();
        } catch (Exception e) {
            logger.error("Error uploading image", e);
            return Message.builder()
                .content("Error uploading image: " + e.getMessage())
                .type(MessageType.red)
                .build();
        }
    }
}