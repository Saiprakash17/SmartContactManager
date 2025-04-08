package com.scm.contactmanager.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;

import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {
   
    // find the contact by user
    Page<Contact> findByUser(User user, Pageable pageable);

    // custom query method
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId")
    List<Contact> findByUserId(@Param("userId") String userId);

    Page<Contact> findByUserAndNameContaining(User user, String nameKeyword, PageRequest pageable);

    Page<Contact> findByUserAndEmailContaining(User user, String emailKeyword, PageRequest pageable);

    Page<Contact> findByUserAndPhoneNumberContaining(User user, String phoneNumberKeyword, PageRequest pageable);

}
