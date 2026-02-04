package com.scm.contactmanager.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;

import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {

    // Find the contact by user with entity graph to prevent N+1 queries
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUser(User user, Pageable pageable);

    // Custom query method with EntityGraph
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    @Query("SELECT c FROM Contact c WHERE c.user.id = :userId")
    List<Contact> findByUserId(@Param("userId") String userId);

    // Find by name containing
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndNameContaining(User user, String nameKeyword, Pageable pageable);

    // Find by email containing
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndEmailContaining(User user, String emailKeyword, Pageable pageable);

    // Find by phone number containing
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndPhoneNumberContaining(User user, String phoneNumberKeyword, Pageable pageable);

    // Find by relationship
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    @Query("SELECT c FROM Contact c WHERE c.user = :user AND LOWER(CAST(c.relationship AS string)) LIKE LOWER(CONCAT('%', :relationship, '%'))")
    Page<Contact> findByUserAndRelationshipContainingIgnoreCase(@Param("user") User user, @Param("relationship") String relationship, Pageable pageable);

    // Find favorites by name
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndFavoriteTrueAndNameContaining(User user, String nameKeyword, Pageable pageable);

    // Find favorites by email
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndFavoriteTrueAndEmailContaining(User user, String emailKeyword, Pageable pageable);

    // Find favorites by phone
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndFavoriteTrueAndPhoneNumberContaining(User user, String phoneNumberKeyword, Pageable pageable);

    // Find favorites by relationship
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    @Query("SELECT c FROM Contact c WHERE c.user = :user AND c.favorite = true AND LOWER(CAST(c.relationship AS string)) LIKE LOWER(CONCAT('%', :relationship, '%'))")
    Page<Contact> findByUserAndFavoriteTrueAndRelationshipContainingIgnoreCase(@Param("user") User user, @Param("relationship") String relationship, Pageable pageable);

    // Find favorite contacts for a user (paginated)
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Page<Contact> findByUserAndFavoriteTrue(User user, Pageable pageable);

    // API search method with EntityGraph
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Contact> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(
        @Param("keyword") String keyword,
        @Param("keyword") String keywordEmail,
        @Param("keyword") String keywordPhone,
        Pageable pageable);

    // Single contact fetch with EntityGraph
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    Optional<Contact> findByIdAndUser(Long id, User user);

    // Get all contacts for user
    @EntityGraph(attributePaths = {"socialLinks", "address"})
    List<Contact> findAllByUser(User user);
}
