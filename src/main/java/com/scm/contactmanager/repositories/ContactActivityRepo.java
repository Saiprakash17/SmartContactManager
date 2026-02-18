package com.scm.contactmanager.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ContactActivity;
import com.scm.contactmanager.entities.User;

@Repository
public interface ContactActivityRepo extends JpaRepository<ContactActivity, Long> {

    List<ContactActivity> findByContactOrderByTimestampDesc(Contact contact);

    List<ContactActivity> findByUserAndTimestampAfter(User user, LocalDateTime timestamp);
}
