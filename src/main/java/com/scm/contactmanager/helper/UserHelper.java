package com.scm.contactmanager.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserHelper {

    private static final Logger logger = LoggerFactory.getLogger(UserHelper.class);

    public static String getEmailOfLoggedInUser(Authentication authentication) {
        if(authentication instanceof OAuth2AuthenticationToken){

            var oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            var clientId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

            var oauth2User = (OAuth2User) authentication.getPrincipal();
            String username = "";

            if(clientId.equals("google")){
                logger.debug("Extracting email from Google OAuth provider");
                Object emailAttr = oauth2User.getAttribute("email");
                username = emailAttr != null ? emailAttr.toString() : "";
            }
            else if(clientId.equals("github")){
                logger.debug("Extracting email from GitHub OAuth provider");
                Object emailAttr = oauth2User.getAttribute("email");
                Object loginAttr = oauth2User.getAttribute("login");
                if (emailAttr != null) {
                    username = emailAttr.toString();
                } else if (loginAttr != null) {
                    username = loginAttr.toString() + "@gmail.com";
                } else {
                    username = "";
                }
            }
            return username;
        }
        else{
            logger.debug("Extracting email from normal login");
            return authentication.getName();
        }
    }

    public static String getLinkForUserVerification(String email, String verifyToken){
        StringBuilder link = new StringBuilder();
        link.append(AppConstants.DOMAIN);
        link.append("/auth/verify-email?email=");
        link.append(email);
        link.append("&token=");
        link.append(verifyToken);
        return link.toString();
    }

    public static String getLinkForPasswordReset(String token){
        StringBuilder link = new StringBuilder();
        link.append(AppConstants.DOMAIN);
        link.append("/reset-password?token=");
        link.append(token);
        return link.toString();
    }
}
