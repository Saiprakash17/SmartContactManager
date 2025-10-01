package com.scm.contactmanager.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;

import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {

    

    // Find the contact by user
    Page<Contact> findByUser(User user, Pageable pageable);

    // Custom query method
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId")
    List<Contact> findByUserId(@Param("userId") String userId);

    Page<Contact> findByUserAndNameContaining(User user, String nameKeyword, Pageable pageable);

    Page<Contact> findByUserAndEmailContaining(User user, String emailKeyword, Pageable pageable);

    Page<Contact> findByUserAndPhoneNumberContaining(User user, String phoneNumberKeyword, Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.user = :user AND LOWER(CAST(c.relationship AS string)) LIKE LOWER(CONCAT('%', :relationship, '%'))")
    Page<Contact> findByUserAndRelationshipContainingIgnoreCase(@Param("user") User user, @Param("relationship") String relationship, Pageable pageable);

    

    Page<Contact> findByUserAndFavoriteTrueAndNameContaining(User user, String nameKeyword, Pageable pageable);

    Page<Contact> findByUserAndFavoriteTrueAndEmailContaining(User user, String emailKeyword, Pageable pageable);

    Page<Contact> findByUserAndFavoriteTrueAndPhoneNumberContaining(User user, String phoneNumberKeyword, Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.user = :user AND c.favorite = true AND LOWER(CAST(c.relationship AS string)) LIKE LOWER(CONCAT('%', :relationship, '%'))")
    Page<Contact> findByUserAndFavoriteTrueAndRelationshipContainingIgnoreCase(@Param("user") User user, @Param("relationship") String relationship, Pageable pageable);

    // Find favorite contacts for a user (paginated)
    Page<Contact> findByUserAndFavoriteTrue(User user, Pageable pageable);

    // API search method
    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Contact> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
        @Param("keyword") String keyword,
        @Param("keyword") String keywordEmail,
        @Param("keyword") String keywordPhone,
        Pageable pageable);
}
