package com.scm.contactmanager.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scm.contactmanager.entities.CommunicationLog;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;

@Repository
public interface CommunicationLogRepo extends JpaRepository<CommunicationLog, Long> {

    /**
     * Find all communication logs for a specific contact, ordered by timestamp descending
     */
    List<CommunicationLog> findByContactOrderByTimestampDesc(Contact contact);

    /**
     * Find paginated communication logs for a specific contact
     */
    Page<CommunicationLog> findByContactOrderByTimestampDesc(Contact contact, Pageable pageable);

    /**
     * Find all communication logs for a user within a date range
     */
    @Query("SELECT cl FROM CommunicationLog cl WHERE cl.user = :user AND cl.timestamp BETWEEN :startDate AND :endDate ORDER BY cl.timestamp DESC")
    List<CommunicationLog> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find communication logs that need follow-up (nextFollowUp is not null and is in past/present)
     */
    @Query("SELECT cl FROM CommunicationLog cl WHERE cl.user = :user AND cl.nextFollowUp IS NOT NULL AND cl.nextFollowUp <= CURRENT_TIMESTAMP ORDER BY cl.nextFollowUp ASC")
    List<CommunicationLog> findPendingFollowups(@Param("user") User user);

    /**
     * Count communication logs for a specific contact
     */
    long countByContact(Contact contact);

    /**
     * Find recent communications for a contact
     */
    @Query("SELECT cl FROM CommunicationLog cl WHERE cl.contact = :contact AND cl.timestamp >= :startDate ORDER BY cl.timestamp DESC")
    List<CommunicationLog> findRecentCommunications(@Param("contact") Contact contact, @Param("startDate") LocalDateTime startDate);
}
