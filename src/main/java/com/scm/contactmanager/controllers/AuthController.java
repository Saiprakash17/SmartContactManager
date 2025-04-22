package com.scm.contactmanager.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;
import com.scm.contactmanager.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @RequestMapping("/verify-email")
    public String verifyEmail(@PathParam("email") String email, @PathParam("token") String token, HttpSession session) {
        User user = userService.getUserByEmailAndVerifyToken(email, token);
        if (user != null) {
            user.setVerifyToken(null); 
            user.setEmailVerified(true);
            user.setEnabled(true);
            userService.updateUser(user);
            session.setAttribute("message", Message.builder()
                        .type(MessageType.green)
                        .content("You email is verified. Now you can login  ")
                        .build());
            return "redirect:/login";
        } else {
            session.setAttribute("message", Message.builder()
                        .type(MessageType.red)
                        .content("Invalid verification link or email already verified.")
                        .build());
            return "redirect:/login";
        }
    }
}
