package com.scm.contactmanager.controllers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.UserHelper;
import com.scm.contactmanager.services.UserService;

@ControllerAdvice
public class RootController {

    Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @ModelAttribute
    public void addLoggedInUserInModel(Model model, Authentication authentication) {

        System.out.println("Adding logged in user in model");

        if(authentication == null) {
            return;
        }

        String username = UserHelper.getEmailOfLoggedInUser(authentication);
        logger.info("Username: " + username);

        User user = userService.getUserByEmail(username);

        model.addAttribute("loggedInUser", user);
    }

}
