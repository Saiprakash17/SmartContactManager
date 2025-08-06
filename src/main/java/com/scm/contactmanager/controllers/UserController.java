package com.scm.contactmanager.controllers;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ChangePasswordForm;
import com.scm.contactmanager.forms.ProfileForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/user")
public class UserController {

    Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @Autowired
    ImageService imageService;

    //dashboard
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard() {
        System.out.println("Dashboard page requested");
        return "user/dashboard";
    }

    //profile
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Model model) {
        System.out.println("Profile page requested");
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            // handle missing user gracefully
            return "redirect:/login";
        }
        ProfileForm profileForm = new ProfileForm(user.getName(), user.getPassword(), user.getPhoneNumber(), user.getAbout(), null, user.getImageUrl());
        model.addAttribute("profileForm", profileForm);
        ChangePasswordForm changePasswordForm = new ChangePasswordForm();
        model.addAttribute("changePasswordForm", changePasswordForm);
        return "user/profile";
    }

    //profile update
    @PostMapping("/profile/update")
    public String updateProfile(
        @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
        Model model, 
        BindingResult bindingResult,
        HttpSession session) {
        
        System.out.println("Profile update page requested");
        System.out.println("ProfileForm: " + profileForm.toString());

        if (bindingResult.hasErrors()) {
            System.out.println("Binding result has errors: " + bindingResult.getAllErrors());
            model.addAttribute("profileForm", profileForm);
            ChangePasswordForm changePasswordForm = new ChangePasswordForm();
            model.addAttribute("changePasswordForm", changePasswordForm);
            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());
            return "user/profile";
        }

        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if(!userService.validatePassword(user, profileForm.getCurrentPassword())) {
            model.addAttribute("profileForm", profileForm);
            session.setAttribute("message", Message.builder()
                    .content("Current password is incorrect")
                    .type(MessageType.red)
                    .build());
            return "user/profile";
        } else {
            user.setName(profileForm.getName());
            user.setPhoneNumber(profileForm.getPhoneNumber());
            user.setAbout(profileForm.getAbout());
            if (profileForm.getContactImage() != null && !profileForm.getContactImage().isEmpty()) {
                String fileName = UUID.randomUUID().toString();
                String fileURL = imageService.uploadImage(profileForm.getContactImage(), fileName);
                user.setImageUrl(fileURL);
                user.setCloudinaryImagePublicId(fileName);
            }
            userService.updateUser(user);
            session.setAttribute("message", Message.builder()
                    .content("Profile updated successfully")
                    .type(MessageType.green)
                    .build());
            return "redirect:/user/profile";
        }
    }

    //delete account
    @RequestMapping(value = "/profile/delete", method = RequestMethod.POST)
    public String deleteAccount(Model model, HttpSession session) {
        System.out.println("Delete account page requested");
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        userService.deleteUserById(user.getId());
        session.setAttribute("message",
                Message.builder()
                        .content("Your account has been deleted successfully")
                        .type(MessageType.green)
                        .build());
        return "redirect:/logout";
    }

    //change password
    @RequestMapping(value = "/profile/change-password", method = RequestMethod.POST)
    public String changePassword(@Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
            BindingResult bindingResult, Model model, HttpSession session) {
        System.out.println("Change password page requested");
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            ProfileForm profileForm = new ProfileForm(user.getName(), user.getPassword(), user.getPhoneNumber(), user.getAbout(), null, user.getImageUrl());
            model.addAttribute("profileForm", profileForm);
            session.setAttribute("message", Message.builder()
                    .content("Please correct the following errors")
                    .type(MessageType.red)
                    .build());
            return "user/profile";
        }
        if (!userService.validatePassword(user, changePasswordForm.getCurrentPassword())) {
            ProfileForm profileForm = new ProfileForm(user.getName(), user.getPassword(), user.getPhoneNumber(), user.getAbout(), null, user.getImageUrl());
            model.addAttribute("profileForm", profileForm);
            session.setAttribute("message", Message.builder()
                    .content("Current password is incorrect")
                    .type(MessageType.red)
                    .build());
            return "user/profile";
        } else {
            userService.updatePassword(user, changePasswordForm.getNewPassword());
            session.setAttribute("message", Message.builder()
                    .content("Password updated successfully")
                    .type(MessageType.green)
                    .build());
            return "redirect:/user/profile";
        }
    }
    
}
