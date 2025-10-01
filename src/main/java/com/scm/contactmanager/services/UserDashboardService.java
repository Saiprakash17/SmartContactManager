package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ChangePasswordForm;
import com.scm.contactmanager.forms.ProfileForm;
import com.scm.contactmanager.helper.Message;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

public interface UserDashboardService {
    ProfileForm getProfileForm(User user);
    Message updateProfile(ProfileForm profileForm, User user, BindingResult bindingResult);
    Message deleteAccount(User user);
    Message changePassword(ChangePasswordForm changePasswordForm, User user, BindingResult bindingResult);
    Message handleProfileImageUpload(User user, MultipartFile image);
}