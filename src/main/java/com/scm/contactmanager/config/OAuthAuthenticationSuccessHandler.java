package com.scm.contactmanager.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.scm.contactmanager.entities.Providers;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.AppConstants;
import com.scm.contactmanager.repositories.UserRepo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    Logger logger = org.slf4j.LoggerFactory.getLogger(OAuthAuthenticationSuccessHandler.class);

    @Autowired
    private UserRepo userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuthAuthenticationSuccessHandler");

        var oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        String authrizedClientRegistrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();

        logger.info("Authorized Client Registration Id: " + authrizedClientRegistrationId);

        DefaultOAuth2User oAuthUser = (DefaultOAuth2User)authentication.getPrincipal();

        oAuthUser.getAttributes().forEach((k, v) -> {
                logger.info(k + ": " + v);
            });


        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setRoles(List.of(AppConstants.ROLE_USER));
        user.setEnabled(true);
        user.setPassword("");


        if(authrizedClientRegistrationId.equals("google")){
            user.setEmailVerified(true);
            user.setEmail(oAuthUser.getAttribute("email").toString());
            user.setImageUrl(oAuthUser.getAttribute("picture").toString());
            user.setName(oAuthUser.getAttribute("name").toString());
            user.setProviderUserId(oAuthUser.getName());
            user.setProvider(Providers.GOOGLE);
            user.setAbout("This account is created using google.");

        }


        else if (authrizedClientRegistrationId.equals("github")){
            user.setEmailVerified(true);
            String email = oAuthUser.getAttribute("email") != null ? oAuthUser.getAttribute("email").toString()
                    : oAuthUser.getAttribute("login").toString() + "@gmail.com";

            user.setEmail(email);
            user.setImageUrl(oAuthUser.getAttribute("avatar_url").toString());
            user.setName(oAuthUser.getAttribute("login").toString());
            user.setProviderUserId(oAuthUser.getName());
            user.setProvider(Providers.GITHUB);
            user.setAbout("This account is created using github.");
        }

        else{
            logger.error("Unknown OAuth2 provider: " + authrizedClientRegistrationId);
        }

        // DefaultOAuth2User oAuthUser = (DefaultOAuth2User)authentication.getPrincipal();

        // logger.info("User Name: " + oAuthUser.getName());
        // logger.info("User Authorities: " + oAuthUser.getAuthorities());
        // oAuthUser.getAttributes().forEach((k, v) -> {
        //     logger.info(k + ": " + v);
        // });

        // String email = oAuthUser.getAttribute("email");
        // User user = new User();
        // user.setEmail(email);
        // user.setName(oAuthUser.getAttribute("name"));
        // user.setImageUrl(oAuthUser.getAttribute("picture"));
        // user.setProvider(Providers.GOOGLE);
        // user.setProviderUserId(oAuthUser.getAttribute("sub"));
        // user.setEnabled(true);
        // user.setEmailVerified(true);
        // user.setAbout("Acoount created using Google OAuth2");
        // user.setRoles(List.of(AppConstants.ROLE_USER));
        // user.setId(UUID.randomUUID().toString());
        // user.setPassword("");
        // user.setPhoneNumber("");

        if(!userRepo.findByEmail(user.getEmail()).isPresent()){
            userRepo.save(user);
            logger.info("User saved: " + user.getEmail());
        }


        new DefaultRedirectStrategy().sendRedirect(request, response, "/user/profile");
    }

}
