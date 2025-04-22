package com.scm.contactmanager.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.AppConstants;
import com.scm.contactmanager.helper.ResourseNotFoundException;
import com.scm.contactmanager.helper.UserHelper;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public User saveUser(User user) {
        //user id generation
        String userId = UUID.randomUUID().toString();
        user.setId(userId);

        //password encoding
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //role setting
        user.setRoles(List.of(AppConstants.ROLE_USER));
        

        String verifyToken = UUID.randomUUID().toString();
        user.setVerifyToken(verifyToken);

        logger.info("User saved successfully");
        User savedUser = userRepo.save(user);

        emailService.sendEmail(savedUser.getEmail(), "Verify your email", 
        "Click on the link to verify your email: " + UserHelper.getLinkForUserVerification(savedUser.getEmail(), verifyToken));
        
        return savedUser;
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepo.findById(id);
    }

    @Override
    public Optional<User> updateUser(User user) {
        User newUser = userRepo.findById(user.getId()).orElseThrow(() -> new ResourseNotFoundException("User not found"));
        newUser.setName(user.getName());   
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setImageUrl(user.getImageUrl());
        newUser.setAbout(user.getAbout());
        newUser.setPhoneNumber(user.getPhoneNumber());
        newUser.setEnabled(user.isEnabled());
        newUser.setEmailVerified(user.isEmailVerified());
        newUser.setPhoneVerified(user.isPhoneVerified());
        newUser.setProvider(user.getProvider());
        newUser.setProviderUserId(user.getProviderUserId());
        newUser.setVerifyToken(user.getVerifyToken());
        newUser.setCloudinaryImagePublicId(user.getCloudinaryImagePublicId());

        return Optional.ofNullable(userRepo.save(newUser));

    }

    @Override
    public void deleteUserById(String id) {
        User newUser = userRepo.findById(id).orElseThrow(() -> new ResourseNotFoundException("User not found"));
        userRepo.delete(newUser);
    }

    @Override
    public boolean isUserPresent(String id) {
        User newUser = userRepo.findById(id).orElse(null);
        return newUser != null;
    }

    @Override
    public boolean isUserPresentByEmail(String email) {
        User user = userRepo.findByEmail(email).orElse(null);
        return user != null;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    @Override
    public User getUserByEmailAndVerifyToken(String email,String token) {
        User user = userRepo.findByEmailAndVerifyToken(email, token).orElse(null);
        return user;
    }

    @Override
    public boolean validatePassword(User user, String currentPassword) {
        if (user.getPassword() == null || currentPassword == null) {
            logger.error("User password or current password is null");
            return false;
        }
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);
        } else {
            logger.error("New password is null or empty");
        }
    }  

}
