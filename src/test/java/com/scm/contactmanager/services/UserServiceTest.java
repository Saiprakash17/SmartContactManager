package com.scm.contactmanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.impl.UserServiceImpl;
import com.scm.contactmanager.helper.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-id");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
    }

    @Test
    void shouldSaveUser() {
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = userService.saveUser(testUser);

        assertNotNull(savedUser);
        assertEquals(testUser.getName(), savedUser.getName());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void shouldGetUserByEmail() {
        when(userRepo.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserByEmail(testUser.getEmail());

        assertNotNull(foundUser);
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        verify(userRepo).findByEmail(testUser.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepo.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            userService.getUserByEmail("nonexistent@example.com")
        );

        verify(userRepo).findByEmail("nonexistent@example.com");
    }

    @Test
    void shouldDeleteUser() {
        when(userRepo.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepo).delete(testUser);

        userService.deleteUserById(testUser.getId());

        verify(userRepo).findById(testUser.getId());
        verify(userRepo).delete(testUser);
    }

    @Test
    void shouldUpdateUser() {
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        
        when(userRepo.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(User.class))).thenReturn(updatedUser);

        Optional<User> result = userService.updateUser(updatedUser);

        assertTrue(result.isPresent());
        assertEquals("Updated Name", result.get().getName());
        assertEquals("updated@example.com", result.get().getEmail());
    }

    @Test
    void shouldCheckUserPresenceById() {
        when(userRepo.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        boolean result = userService.isUserPresent(testUser.getId());

        assertTrue(result);
        verify(userRepo).findById(testUser.getId());
    }

    @Test
    void shouldCheckUserPresenceByEmail() {
        when(userRepo.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        boolean result = userService.isUserPresentByEmail(testUser.getEmail());

        assertTrue(result);
        verify(userRepo).findByEmail(testUser.getEmail());
    }

    @Test
    void shouldValidatePassword() {
        String currentPassword = "password";
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        boolean result = userService.validatePassword(testUser, currentPassword);

        assertTrue(result);
        verify(passwordEncoder).matches(currentPassword, testUser.getPassword());
    }

    @Test
    void shouldUpdatePassword() {
        String newPassword = "newPassword";
        String encodedPassword = "encodedNewPassword";
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        userService.updatePassword(testUser, newPassword);

        assertEquals(encodedPassword, testUser.getPassword());
        verify(userRepo).save(testUser);
    }
}
