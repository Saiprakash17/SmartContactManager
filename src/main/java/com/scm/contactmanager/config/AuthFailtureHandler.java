package com.scm.contactmanager.config;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.scm.contactmanager.helper.Message;
import com.scm.contactmanager.helper.MessageType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AuthFailtureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
            
        HttpSession session = request.getSession();
        if(exception instanceof BadCredentialsException){
            session.setAttribute("message", Message.builder()
                    .content("Invalid username or password.")
                    .type(MessageType.red)
                    .build());
            response.sendRedirect(request.getContextPath() + "/login");
        }        
        else if(exception instanceof DisabledException){
            session.setAttribute("message", Message.builder()
                    .content("Your account is disabled. Please contact the administrator.")
                    .type(MessageType.red)
                    .build());
            response.sendRedirect(request.getContextPath() + "/login");
        }
        else if(exception instanceof InternalAuthenticationServiceException){
            session.setAttribute("message", Message.builder()
                    .content("Internal server error. Please try again later.")
                    .type(MessageType.red)
                    .build());
            response.sendRedirect(request.getContextPath() + "/login");
        }
        else{
            session.setAttribute("message", Message.builder()
                    .content("Authentication failed. Please try again.")
                    .type(MessageType.red)
                    .build());
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

}
