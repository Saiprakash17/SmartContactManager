package com.scm.contactmanager.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;
    private Contact contact;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .password("password@123")
                .imageUrl("https://example.com/image.jpg")
                .about("Software Engineer")
                .phoneNumber("+1234567890")
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(false)
                .provider(Providers.SELF)
                .providerUserId(null)
                .verifyToken("token123")
                .cloudinaryImagePublicId("public_id_123")
                .build();
        
        contact = Contact.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .build();
    }

    @Nested
    @DisplayName("User Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create user with NoArgsConstructor")
        void testNoArgsConstructor() {
            User newUser = new User();
            assertNotNull(newUser);
            assertNull(newUser.getId());
            assertNull(newUser.getName());
        }

        @Test
        @DisplayName("Should create user with AllArgsConstructor")
        void testAllArgsConstructor() {
            List<String> roles = List.of("ROLE_USER");
            List<Contact> contacts = new ArrayList<>();
            User newUser = new User("user1", "John", "john@test.com", "pwd", "img.jpg", "about", 
                    "+123456", true, true, false, Providers.SELF, null, contacts, roles, "token", "pid");
            
            assertEquals("user1", newUser.getId());
            assertEquals("John", newUser.getName());
            assertEquals("john@test.com", newUser.getEmail());
        }

        @Test
        @DisplayName("Should create user with Builder")
        void testBuilder() {
            assertNotNull(user);
            assertEquals("user123", user.getId());
            assertEquals("John Doe", user.getName());
            assertEquals("john@example.com", user.getEmail());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            user.setId("newId");
            assertEquals("newId", user.getId());
        }

        @Test
        @DisplayName("Should get and set name")
        void testNameGetterSetter() {
            user.setName("Jane Doe");
            assertEquals("Jane Doe", user.getName());
        }

        @Test
        @DisplayName("Should get and set email")
        void testEmailGetterSetter() {
            user.setEmail("newemail@example.com");
            assertEquals("newemail@example.com", user.getEmail());
        }

        @Test
        @DisplayName("Should get and set password")
        void testPasswordGetterSetter() {
            user.setPassword("newPassword123");
            assertEquals("newPassword123", user.getPassword());
        }

        @Test
        @DisplayName("Should get and set imageUrl")
        void testImageUrlGetterSetter() {
            user.setImageUrl("https://new-image.jpg");
            assertEquals("https://new-image.jpg", user.getImageUrl());
        }

        @Test
        @DisplayName("Should get and set about")
        void testAboutGetterSetter() {
            user.setAbout("New description");
            assertEquals("New description", user.getAbout());
        }

        @Test
        @DisplayName("Should get and set phoneNumber")
        void testPhoneNumberGetterSetter() {
            user.setPhoneNumber("+9876543210");
            assertEquals("+9876543210", user.getPhoneNumber());
        }

        @Test
        @DisplayName("Should get and set enabled status")
        void testEnabledGetterSetter() {
            user.setEnabled(false);
            assertFalse(user.isEnabled());
        }

        @Test
        @DisplayName("Should get and set emailVerified")
        void testEmailVerifiedGetterSetter() {
            user.setEmailVerified(false);
            assertFalse(user.isEmailVerified());
        }

        @Test
        @DisplayName("Should get and set phoneVerified")
        void testPhoneVerifiedGetterSetter() {
            user.setPhoneVerified(true);
            assertTrue(user.isPhoneVerified());
        }

        @Test
        @DisplayName("Should get and set provider")
        void testProviderGetterSetter() {
            user.setProvider(Providers.GOOGLE);
            assertEquals(Providers.GOOGLE, user.getProvider());
        }

        @Test
        @DisplayName("Should get and set providerUserId")
        void testProviderUserIdGetterSetter() {
            user.setProviderUserId("google123");
            assertEquals("google123", user.getProviderUserId());
        }

        @Test
        @DisplayName("Should get and set contacts list")
        void testContactsGetterSetter() {
            List<Contact> contacts = new ArrayList<>();
            contacts.add(contact);
            user.setContacts(contacts);
            assertEquals(1, user.getContacts().size());
            assertSame(contact, user.getContacts().get(0));
        }

        @Test
        @DisplayName("Should get and set roles list")
        void testRolesGetterSetter() {
            List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
            user.setRoles(roles);
            assertEquals(2, user.getRoles().size());
            assertTrue(user.getRoles().contains("ROLE_USER"));
            assertTrue(user.getRoles().contains("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("Should get and set verifyToken")
        void testVerifyTokenGetterSetter() {
            user.setVerifyToken("newToken");
            assertEquals("newToken", user.getVerifyToken());
        }

        @Test
        @DisplayName("Should get and set cloudinaryImagePublicId")
        void testCloudinaryImagePublicIdGetterSetter() {
            user.setCloudinaryImagePublicId("new_public_id");
            assertEquals("new_public_id", user.getCloudinaryImagePublicId());
        }
    }

    @Nested
    @DisplayName("UserDetails Implementation Tests")
    class UserDetailsTests {
        
        @Test
        @DisplayName("Should return authorities from roles")
        void testGetAuthorities() {
            user.setRoles(List.of("ROLE_USER", "ROLE_ADMIN"));
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            
            assertNotNull(authorities);
            assertEquals(2, authorities.size());
            
            List<String> authorityNames = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            assertTrue(authorityNames.contains("ROLE_USER"));
            assertTrue(authorityNames.contains("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("Should return email as username")
        void testGetUsername() {
            assertEquals("john@example.com", user.getUsername());
        }

        @Test
        @DisplayName("Should return true for isEnabled when enabled is true")
        void testIsEnabledTrue() {
            user.setEnabled(true);
            assertTrue(user.isEnabled());
        }

        @Test
        @DisplayName("Should return false for isEnabled when enabled is false")
        void testIsEnabledFalse() {
            user.setEnabled(false);
            assertFalse(user.isEnabled());
        }

        @Test
        @DisplayName("Should return true for isAccountNonExpired")
        void testIsAccountNonExpired() {
            assertTrue(user.isAccountNonExpired());
        }

        @Test
        @DisplayName("Should return true for isAccountNonLocked")
        void testIsAccountNonLocked() {
            assertTrue(user.isAccountNonLocked());
        }

        @Test
        @DisplayName("Should return true for isCredentialsNonExpired")
        void testIsCredentialsNonExpired() {
            assertTrue(user.isCredentialsNonExpired());
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {
        
        @Test
        @DisplayName("Should have enabled as false by default")
        void testDefaultEnabled() {
            User newUser = new User();
            assertFalse(newUser.isEnabled());
        }

        @Test
        @DisplayName("Should have emailVerified as false by default")
        void testDefaultEmailVerified() {
            User newUser = new User();
            assertFalse(newUser.isEmailVerified());
        }

        @Test
        @DisplayName("Should have phoneVerified as false by default")
        void testDefaultPhoneVerified() {
            User newUser = new User();
            assertFalse(newUser.isPhoneVerified());
        }

        @Test
        @DisplayName("Should have provider as SELF by default")
        void testDefaultProvider() {
            User newUser = new User();
            assertEquals(Providers.SELF, newUser.getProvider());
        }

        @Test
        @DisplayName("Should have empty contacts list by default")
        void testDefaultContacts() {
            User newUser = new User();
            assertNotNull(newUser.getContacts());
            assertEquals(0, newUser.getContacts().size());
        }

        @Test
        @DisplayName("Should have empty roles list by default")
        void testDefaultRoles() {
            User newUser = new User();
            assertNotNull(newUser.getRoles());
            assertEquals(0, newUser.getRoles().size());
        }
    }

    @Nested
    @DisplayName("Relationships Tests")
    class RelationshipsTests {
        
        @Test
        @DisplayName("Should add contact to user")
        void testAddContact() {
            user.getContacts().add(contact);
            assertEquals(1, user.getContacts().size());
            assertTrue(user.getContacts().contains(contact));
        }

        @Test
        @DisplayName("Should remove contact from user")
        void testRemoveContact() {
            user.getContacts().add(contact);
            user.getContacts().remove(contact);
            assertEquals(0, user.getContacts().size());
        }

        @Test
        @DisplayName("Should maintain multiple contacts")
        void testMultipleContacts() {
            Contact contact2 = Contact.builder()
                    .id(2L)
                    .name("Bob Smith")
                    .email("bob@example.com")
                    .build();
            
            user.getContacts().add(contact);
            user.getContacts().add(contact2);
            
            assertEquals(2, user.getContacts().size());
            assertTrue(user.getContacts().contains(contact));
            assertTrue(user.getContacts().contains(contact2));
        }
    }

    @Nested
    @DisplayName("Null & Empty Tests")
    class NullAndEmptyTests {
        
        @Test
        @DisplayName("Should handle null values in setters")
        void testNullValues() {
            user.setName(null);
            user.setEmail(null);
            user.setPassword(null);
            
            assertNull(user.getName());
            assertNull(user.getEmail());
            assertNull(user.getPassword());
        }

        @Test
        @DisplayName("Should handle empty string values")
        void testEmptyStringValues() {
            user.setName("");
            user.setEmail("");
            
            assertEquals("", user.getName());
            assertEquals("", user.getEmail());
        }

        @Test
        @DisplayName("Should handle null providerUserId")
        void testNullProviderUserId() {
            user.setProviderUserId(null);
            assertNull(user.getProviderUserId());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {
        
        @Test
        @DisplayName("Should support SELF provider")
        void testSelfProvider() {
            user.setProvider(Providers.SELF);
            assertEquals(Providers.SELF, user.getProvider());
        }

        @Test
        @DisplayName("Should support GOOGLE provider")
        void testGoogleProvider() {
            user.setProvider(Providers.GOOGLE);
            assertEquals(Providers.GOOGLE, user.getProvider());
        }

        @Test
        @DisplayName("Should support FACEBOOK provider")
        void testFacebookProvider() {
            user.setProvider(Providers.FACEBOOK);
            assertEquals(Providers.FACEBOOK, user.getProvider());
        }

        @Test
        @DisplayName("Should support GITHUB provider")
        void testGithubProvider() {
            user.setProvider(Providers.GITHUB);
            assertEquals(Providers.GITHUB, user.getProvider());
        }
    }

    @Nested
    @DisplayName("Builder Default Values Tests")
    class BuilderDefaultValuesTests {
        
        @Test
        @DisplayName("Builder should initialize default collections")
        void testBuilderDefaultCollections() {
            User newUser = User.builder().build();
            assertNotNull(newUser.getContacts());
            assertNotNull(newUser.getRoles());
        }

        @Test
        @DisplayName("Builder should set provider to SELF by default")
        void testBuilderDefaultProvider() {
            User newUser = User.builder().build();
            assertEquals(Providers.SELF, newUser.getProvider());
        }

        @Test
        @DisplayName("Builder should set enabled to false by default")
        void testBuilderDefaultEnabled() {
            User newUser = User.builder().build();
            assertFalse(newUser.isEnabled());
        }
    }

    @Nested
    @DisplayName("Combined Functionality Tests")
    class CombinedFunctionalityTests {
        
        @Test
        @DisplayName("Should handle full user lifecycle")
        void testCompleteUserLifecycle() {
            User newUser = User.builder()
                    .id("user1")
                    .name("Alice")
                    .email("alice@example.com")
                    .password("pwd123")
                    .enabled(false)
                    .provider(Providers.SELF)
                    .build();
            
            newUser.setEnabled(true);
            newUser.setRoles(List.of("ROLE_USER"));
            newUser.getContacts().add(contact);
            
            assertTrue(newUser.isEnabled());
            assertEquals(1, newUser.getRoles().size());
            assertEquals(1, newUser.getContacts().size());
            assertEquals("alice@example.com", newUser.getUsername());
        }

        @Test
        @DisplayName("Should handle multiple role assignments")
        void testMultipleRoleAssignment() {
            user.setRoles(new ArrayList<>(List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR")));
            
            assertEquals(3, user.getRoles().size());
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            assertEquals(3, authorities.size());
        }

        @Test
        @DisplayName("Should update user information")
        void testUpdateUserInfo() {
            String oldEmail = user.getEmail();
            user.setEmail("newemail@example.com");
            user.setName("New Name");
            user.setPhoneNumber("+9999999999");
            
            assertNotEquals(oldEmail, user.getEmail());
            assertEquals("New Name", user.getName());
            assertEquals("+9999999999", user.getPhoneNumber());
        }
    }
}
