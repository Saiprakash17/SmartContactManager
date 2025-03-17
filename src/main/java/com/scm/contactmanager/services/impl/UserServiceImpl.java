package com.scm.contactmanager.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.helper.ResourseNotFountException;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public User saveUser(User user) {
        //user id generation
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        logger.info("User saved successfully");
        return userRepo.save(user);
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepo.findById(id);
    }

    @Override
    public Optional<User> updateUser(User user) {
        User newUser = userRepo.findById(user.getId()).orElseThrow(() -> new ResourseNotFountException("User not found"));
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

        return Optional.ofNullable(userRepo.save(newUser));

    }

    @Override
    public void deleteUserById(String id) {
        User newUser = userRepo.findById(id).orElseThrow(() -> new ResourseNotFountException("User not found"));
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


}
