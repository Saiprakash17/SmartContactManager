package com.scm.contactmanager.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scm.contactmanager.entities.User;


@Repository
public interface UserRepo extends JpaRepository<User, String>{

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmailAndVerifyToken(String email,String token);
}
