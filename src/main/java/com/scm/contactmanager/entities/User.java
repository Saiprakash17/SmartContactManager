package com.scm.contactmanager.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "user")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    private String id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String name;

    @Column(name = "user_email", unique = true, length = 100)
    private String email;

    @Column(name = "user_password", length = 100)
    private String password;

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String about;

    @Column(name = "phone_number", length = 100)
    private String phoneNumber;

    private boolean enabled = false;
    private boolean emailVerified = false;
    private boolean phoneVerified = false;

    @Enumerated(jakarta.persistence.EnumType.STRING)   
    // SELF, GOOGLE, FACEBOOK, GITHUB
    private Providers provider = Providers.SELF;
    private String providerUserId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();
    
}
