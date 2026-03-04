package com.scm.contactmanager.controllers;

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

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @RequestMapping("/verify-email")
    public String verifyEmail(@PathParam("email") String email, @PathParam("token") String token, HttpSession session) {
        if (email == null || email.isEmpty() || token == null || token.isEmpty()) {
            session.setAttribute("message", Message.builder()
                        .type(MessageType.red)
                        .content("Invalid email verification link")
                        .build());
            return "redirect:/login";
        }
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
