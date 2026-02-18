package com.scm.contactmanager.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "important_dates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportantDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(nullable = false, length = 100)
    private String name; // "Birthday", "Anniversary", "Wedding Day"

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "BIT(1) DEFAULT 1")
    private Boolean notificationEnabled = true;

    @Column
    private Integer daysBeforeNotify = 7;

    @Column
    private LocalDateTime lastNotified;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
