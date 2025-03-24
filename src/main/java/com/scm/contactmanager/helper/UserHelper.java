package com.scm.contactmanager.helper;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserHelper {

    public static String getEmailOfLoggedInUser(Authentication authentication) {
        if(authentication instanceof OAuth2AuthenticationToken){

            var oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            var clientId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            var oauth2User = (OAuth2User) authentication.getPrincipal();
            String username = "";

            if(clientId.equals("google")){
                System.out.println("Getting email from google");
                username = oauth2User.getAttribute("email");
            }
            else if(clientId.equals("github")){
                System.out.println("Getting email from github");
                username = oauth2User.getAttribute("email") != null ? oauth2User.getAttribute("email").toString()
                        : oauth2User.getAttribute("login").toString() + "@gmail.com";
            }
            return username;
        }
        else{
            System.out.println("Getting email from normal login");
            return authentication.getName();
        }
    }
}
