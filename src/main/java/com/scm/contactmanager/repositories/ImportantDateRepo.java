package com.scm.contactmanager.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ImportantDate;

@Repository
public interface ImportantDateRepo extends JpaRepository<ImportantDate, Long> {

    List<ImportantDate> findByContact(Contact contact);

    @Query("SELECT id FROM ImportantDate id WHERE id.date BETWEEN :startDate AND :endDate " +
           "AND id.notificationEnabled = true")
    List<ImportantDate> findUpcomingDates(@Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
}
