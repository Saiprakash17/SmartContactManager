package com.scm.contactmanager.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.EmailService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Implementation Tests")
class UserServiceImplTest {

    @Mock
    private UserRepo userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .password("hashedPassword123")
                .about("Software Developer")
                .enabled(true)
                .emailVerified(true)
                .phoneNumber("1234567890")
                .build();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should save user successfully")
        void testSaveUserSuccess() {
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

            User savedUser = userService.saveUser(testUser);

            assertNotNull(savedUser);
            assertEquals("john@example.com", savedUser.getEmail());
            assertEquals("John Doe", savedUser.getName());
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should handle null user")
        void testSaveNullUser() {
            assertThrows(Exception.class, () -> {
                userService.saveUser(null);
            });
        }

        @Test
        @DisplayName("Should save user with all fields")
        void testSaveUserWithAllFields() {
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
            
            User savedUser = userService.saveUser(testUser);
            assertEquals("Software Developer", savedUser.getAbout());
        }
    }

    @Nested
    @DisplayName("Read User Tests")
    class ReadUserTests {

        @Test
        @DisplayName("Should get user by ID")
        void testGetUserById() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserById("user123");

            assertTrue(result.isPresent());
            assertEquals("john@example.com", result.get().getEmail());
            verify(userRepository, times(1)).findById("user123");
        }

        @Test
        @DisplayName("Should return empty when user not found")
        void testGetUserByIdNotFound() {
            when(userRepository.findById("notfound")).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserById("notfound");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should get user by email")
        void testGetUserByEmail() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

            User result = userService.getUserByEmail("john@example.com");

            assertNotNull(result);
            assertEquals("john@example.com", result.getEmail());
            verify(userRepository, times(1)).findByEmail("john@example.com");
        }

        @Test
        @DisplayName("Should get all users")
        void testGetAllUsers() {
            List<User> users = List.of(testUser);
            when(userRepository.findAll()).thenReturn(users);

            List<User> result = userService.getAllUsers();

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(userRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should check if user exists by ID")
        void testIsUserPresent() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserById("user123");

            assertTrue(result.isPresent());
            verify(userRepository, times(1)).findById("user123");
        }

        @Test
        @DisplayName("Should check if user exists by email")
        void testIsUserPresentByEmail() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

            User result = userService.getUserByEmail("john@example.com");

            assertNotNull(result);
            assertEquals("john@example.com", result.getEmail());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void testUpdateUserSuccess() {
            testUser.setName("Jane Doe");
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            Optional<User> result = userService.updateUser(testUser);

            assertTrue(result.isPresent());
            assertEquals("Jane Doe", result.get().getName());
            verify(userRepository, times(1)).save(testUser);
        }

        @Test
        @DisplayName("Should update user email")
        void testUpdateUserEmail() {
            testUser.setEmail("newemail@example.com");
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            Optional<User> result = userService.updateUser(testUser);

            assertTrue(result.isPresent());
            assertEquals("newemail@example.com", result.get().getEmail());
        }

        @Test
        @DisplayName("Should update user profile")
        void testUpdateUserProfile() {
            testUser.setAbout("Senior Developer");
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            Optional<User> result = userService.updateUser(testUser);

            assertTrue(result.isPresent());
            assertEquals("Senior Developer", result.get().getAbout());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user by ID")
        void testDeleteUserById() {
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).delete(testUser);

            assertDoesNotThrow(() -> userService.deleteUserById("user123"));

            verify(userRepository, times(1)).delete(testUser);
        }

        @Test
        @DisplayName("Should handle delete non-existent user")
        void testDeleteNonExistentUser() {
            when(userRepository.findById("notfound")).thenReturn(Optional.empty());

            assertThrows(Exception.class, () -> userService.deleteUserById("notfound"));
        }
    }

    @Nested
    @DisplayName("Password Management Tests")
    class PasswordManagementTests {

        @Test
        @DisplayName("Should validate correct password")
        void testValidatePasswordSuccess() {
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

            boolean result = userService.validatePassword(testUser, "password123");

            assertTrue(result);
            verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
        }

        @Test
        @DisplayName("Should reject incorrect password")
        void testValidatePasswordFailure() {
            when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

            boolean result = userService.validatePassword(testUser, "wrongpassword");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should update user password")
        void testUpdatePassword() {
            when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            assertDoesNotThrow(() -> userService.updatePassword(testUser, "newPassword123"));

            verify(passwordEncoder, times(1)).encode("newPassword123");
        }
    }

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerificationTests {

        @Test
        @DisplayName("Should get user by email with verification token")
        void testGetUserByEmailAndVerifyToken() {
            when(userRepository.findByEmailAndVerifyToken("john@example.com", "token123"))
                    .thenReturn(Optional.of(testUser));

            User result = userService.getUserByEmailAndVerifyToken("john@example.com", "token123");

            assertNotNull(result);
            assertEquals("john@example.com", result.getEmail());
        }

        @Test
        @DisplayName("Should return null for invalid token")
        void testGetUserWithInvalidToken() {
            when(userRepository.findByEmailAndVerifyToken("john@example.com", "invalidtoken"))
                    .thenReturn(Optional.empty());

            User result = userService.getUserByEmailAndVerifyToken("john@example.com", "invalidtoken");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Should complete registration flow")
        void testRegistrationFlow() {
            User newUser = User.builder()
                    .id("user456")
                    .email("new@example.com")
                    .password("hashedPassword")
                    .name("New User")
                    .enabled(false)
                    .emailVerified(false)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(newUser);
            when(userRepository.findById("user456")).thenReturn(Optional.of(newUser));

            userService.saveUser(newUser);
            Optional<User> retrievedUser = userService.getUserById("user456");

            assertTrue(retrievedUser.isPresent());
            assertEquals(newUser.getEmail(), retrievedUser.get().getEmail());
        }

        @Test
        @DisplayName("Should complete login flow")
        void testLoginFlow() {
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

            User user = userService.getUserByEmail("john@example.com");
            boolean passwordValid = userService.validatePassword(user, "password123");

            assertTrue(passwordValid);
            assertEquals("john@example.com", user.getEmail());
        }

        @Test
        @DisplayName("Should complete profile update flow")
        void testProfileUpdateFlow() {
            testUser.setAbout("Updated Profile");
            when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            Optional<User> result = userService.updateUser(testUser);

            assertTrue(result.isPresent());
            assertEquals("Updated Profile", result.get().getAbout());
        }
    }
}
