package com.scm.contactmanager.controllers;

import java.security.Principal;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.UserHelper;
import com.scm.contactmanager.services.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    

    //dashboard
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard() {
        System.out.println("Dashboard page requested");

        return "user/dashboard";
    }

    //profile
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile() {
        System.out.println("Profile page requested");

        

        return "user/profile";
    }
}
