package com.scm.contactmanager.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

        DefaultOAuth2User oAuthUser = (DefaultOAuth2User)authentication.getPrincipal();

        logger.info("User Name: " + oAuthUser.getName());
        logger.info("User Authorities: " + oAuthUser.getAuthorities());
        oAuthUser.getAttributes().forEach((k, v) -> {
            logger.info(k + ": " + v);
        });

        String email = oAuthUser.getAttribute("email");
        User user = new User();
        user.setEmail(email);
        user.setName(oAuthUser.getAttribute("name"));
        user.setImageUrl(oAuthUser.getAttribute("picture"));
        user.setProvider(Providers.GOOGLE);
        user.setProviderUserId(oAuthUser.getAttribute("sub"));
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setAbout("Acoount created using Google OAuth2");
        user.setRoles(List.of(AppConstants.ROLE_USER));
        user.setId(UUID.randomUUID().toString());
        user.setPassword("");
        user.setPhoneNumber("");

        if(!userRepo.findByEmail(email).isPresent()){
            userRepo.save(user);
            logger.info("User saved: " + email);
        }


        new DefaultRedirectStrategy().sendRedirect(request, response, "/user/profile");
    }

}
