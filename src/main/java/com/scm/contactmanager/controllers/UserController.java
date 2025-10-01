package com.scm.contactmanager.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ChangePasswordForm;
import com.scm.contactmanager.forms.ProfileForm;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.UserDashboardService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
@RequestMapping("/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDashboardService userDashboardService;

    //dashboard
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard() {
        logger.info("Dashboard page requested");
        return "user/dashboard";
    }

    //profile
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Model model) {
        logger.info("Profile page requested");
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            ProfileForm profileForm = userDashboardService.getProfileForm(user);
            model.addAttribute("profileForm", profileForm);
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            return "user/profile";
        } catch (Exception e) {
            logger.error("Error loading profile", e);
            return "redirect:/login";
        }
    }

    //profile update
    @PostMapping("/profile/update")
    public String updateProfile(
        @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
        Model model, 
        BindingResult bindingResult,
        HttpSession session) {
        
        logger.info("Profile update requested");
        
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Message message = userDashboardService.updateProfile(profileForm, user, bindingResult);
        session.setAttribute("message", message);

        if (message.getType() == MessageType.red) {
            model.addAttribute("profileForm", profileForm);
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            return "user/profile";
        }

        return "redirect:/user/profile";
    }

    //delete account
    @RequestMapping(value = "/profile/delete", method = RequestMethod.POST)
    public String deleteAccount(Model model, HttpSession session) {
        logger.info("Delete account requested");
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Message message = userDashboardService.deleteAccount(user);
        session.setAttribute("message", message);

        return "redirect:/logout";
    }

    //change password
    @RequestMapping(value = "/profile/change-password", method = RequestMethod.POST)
    public String changePassword(
        @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
        BindingResult bindingResult, 
        Model model, 
        HttpSession session) {
        
        logger.info("Change password requested");
        User user = (User) model.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Message message = userDashboardService.changePassword(changePasswordForm, user, bindingResult);
        session.setAttribute("message", message);

        if (message.getType() == MessageType.red) {
            model.addAttribute("profileForm", userDashboardService.getProfileForm(user));
            return "user/profile";
        }

        return "redirect:/user/profile";
    }
}
