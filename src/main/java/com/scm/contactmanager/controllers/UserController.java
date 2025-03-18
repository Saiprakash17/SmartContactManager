package com.scm.contactmanager.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/user")
public class UserController {

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
