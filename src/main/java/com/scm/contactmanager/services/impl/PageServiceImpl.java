package com.scm.contactmanager.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.scm.contactmanager.entities.PasswordResetToken;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.FeedbackForm;
import com.scm.contactmanager.forms.ResetPasswordForm;
import com.scm.contactmanager.forms.UserForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.helper.UserHelper;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.PageService;
import com.scm.contactmanager.services.PasswordResetTokenService;
import com.scm.contactmanager.services.UserService;

import java.util.Optional;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @Override
    public Message registerUser(UserForm userForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Message.builder()
                .content("Please correct the form errors")
                .type(MessageType.red)
                .build();
        }

        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            return Message.builder()
                .content("Password and Confirm Password do not match")
                .type(MessageType.red)
                .build();
        }

        try {
            User user = new User();
            user.setName(userForm.getName());   
            user.setEmail(userForm.getEmail());
            user.setPassword(userForm.getPassword());
            user.setImageUrl(null);
            user.setAbout(userForm.getAbout());
            user.setPhoneNumber(userForm.getPhoneNumber());
            user.setEnabled(false);

            userService.saveUser(user);

            return Message.builder()
                .content("Registration Successful!! Verify your email. Link sent to your email")
                .type(MessageType.green)
                .build();
        } catch (Exception e) {
            return Message.builder()
                .content("Error during registration: " + e.getMessage())
                .type(MessageType.red)
                .build();
        }
    }

    @Override
    public Message processFeedback(FeedbackForm feedbackForm) {
        try {
            if (feedbackForm.getEmail() == null || feedbackForm.getEmail().trim().isEmpty() ||
                feedbackForm.getName() == null || feedbackForm.getName().trim().isEmpty() ||
                feedbackForm.getMessage() == null || feedbackForm.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("All fields are required");
            }

            emailService.sendFeedbackEmail(
                feedbackForm.getEmail().trim(),
                "Contact Manager APP Feedback from: " + feedbackForm.getName().trim(),
                feedbackForm.getMessage().trim()
            );

            return Message.builder()
                .type(MessageType.green)
                .content("Thank you for your feedback!")
                .build();
        } catch (Exception e) {
            return Message.builder()
                .type(MessageType.red)
                .content("Error sending feedback: " + e.getMessage())
                .build();
        }
    }

    @Override
    public String processForgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email address is required.";
        }

        try {
            email = email.trim().toLowerCase();
            User user = userService.getUserByEmail(email);
            
            if (user == null) {
                return "No account found with that email address.";
            }

            Optional<PasswordResetToken> existingToken = passwordResetTokenService.findValidTokenForUser(user);
            PasswordResetToken token = existingToken.orElseGet(() -> passwordResetTokenService.createTokenForUser(user));

            String resetLink = UserHelper.getLinkForPasswordReset(token.getToken());
            emailService.sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "Click the link to reset your password: " + resetLink + "\n\nThis link will expire in 24 hours."
            );

            return "SUCCESS";
        } catch (Exception e) {
            return "An error occurred while processing your request. Please try again later.";
        }
    }

    @Override
    public boolean isValidPasswordResetToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenService.findByToken(token);
        return resetTokenOpt.isPresent() && !passwordResetTokenService.isTokenExpired(resetTokenOpt.get());
    }

    @Override
    public Message processResetPassword(String token, ResetPasswordForm form, BindingResult bindingResult) {
        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenService.findByToken(token);
        
        if (resetTokenOpt.isEmpty() || passwordResetTokenService.isTokenExpired(resetTokenOpt.get())) {
            return Message.builder()
                .type(MessageType.red)
                .content("Invalid or expired password reset token.")
                .build();
        }

        if (bindingResult.hasErrors() || !form.getPassword().equals(form.getConfirmPassword())) {
            return Message.builder()
                .type(MessageType.red)
                .content("Please check your password entries and try again.")
                .build();
        }

        try {
            User user = resetTokenOpt.get().getUser();
            userService.updatePassword(user, form.getPassword());
            passwordResetTokenService.deleteToken(resetTokenOpt.get());
            emailService.sendEmail(
                user.getEmail(), 
                "Password Changed", 
                "Your password has been successfully changed. If you did not request this change, please contact support immediately."
            );
            
            return Message.builder()
                .type(MessageType.green)
                .content("Your password has been reset successfully. Please log in with your new password.")
                .build();
        } catch (Exception e) {
            return Message.builder()
                .type(MessageType.red)
                .content("An error occurred while resetting your password. Please try again.")
                .build();
        }
    }
}