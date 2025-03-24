package com.scm.contactmanager.services;

import java.util.List;
import java.util.Optional;

import com.scm.contactmanager.entities.User;

public interface UserService {

    User saveUser(User user);
    Optional<User> getUserById(String id);
    Optional<User> updateUser(User user);
    void deleteUserById(String id);
    boolean isUserPresent(String id);
    boolean isUserPresentByEmail(String email);
    List<User> getAllUsers();
    User getUserByEmail(String email);

}
