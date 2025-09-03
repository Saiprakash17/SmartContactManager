package com.scm.contactmanager.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Entity(name = "user")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

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

    @Default
    private boolean enabled = false;
    @Default
    private boolean emailVerified = false;
    @Default
    private boolean phoneVerified = false;

    @Enumerated(jakarta.persistence.EnumType.STRING)   
    // SELF, GOOGLE, FACEBOOK, GITHUB
    @Default
    private Providers provider = Providers.SELF;
    private String providerUserId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Default
    private List<Contact> contacts = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Default
    private List<String> roles = new ArrayList<>();

    @Column(name = "user_verify_token", length = 1000)
    private String verifyToken;

    private String cloudinaryImagePublicId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAccountNonExpired'");
    }

    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAccountNonLocked'");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCredentialsNonExpired'");
    }
    
}
