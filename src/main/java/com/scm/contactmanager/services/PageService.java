package com.scm.contactmanager.services;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.UserForm;
import com.scm.contactmanager.forms.FeedbackForm;
import com.scm.contactmanager.forms.ResetPasswordForm;
import com.scm.contactmanager.helper.Message;
import org.springframework.validation.BindingResult;

public interface PageService {
    Message registerUser(UserForm userForm, BindingResult bindingResult);
    Message processFeedback(FeedbackForm feedbackForm);
    String processForgotPassword(String email);
    Message processResetPassword(String token, ResetPasswordForm form, BindingResult bindingResult);
    boolean isValidPasswordResetToken(String token);
}