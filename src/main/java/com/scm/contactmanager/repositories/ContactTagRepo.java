package com.scm.contactmanager.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scm.contactmanager.entities.ContactTag;
import com.scm.contactmanager.entities.User;

@Repository
public interface ContactTagRepo extends JpaRepository<ContactTag, Long> {

    List<ContactTag> findByUser(User user);

    boolean existsByNameAndUser(String name, User user);

    Optional<ContactTag> findByIdAndUser(Long id, User user);

    List<ContactTag> findByUserAndNameContainingIgnoreCase(User user, String name);
}
