package com.scm.contactmanager.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "communication_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommunicationType type; // EMAIL, CALL, MEETING, SMS, MESSAGE

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 500)
    private String outcome; // "Positive", "Negative", "Neutral", "Follow-up needed", etc.

    @Column(name = "next_follow_up")
    private LocalDateTime nextFollowUp;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @UpdateTimestamp
    private LocalDateTime lastModified;

    public enum CommunicationType {
        EMAIL,
        CALL,
        MEETING,
        SMS,
        MESSAGE,
        VISIT,
        OTHER
    }
}
