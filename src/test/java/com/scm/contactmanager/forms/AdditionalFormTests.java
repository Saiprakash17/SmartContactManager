package com.scm.contactmanager.forms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserForm Tests")
class UserFormTest {

    private UserForm userForm;

    @BeforeEach
    void setUp() {
        userForm = UserForm.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .about("Software Engineer")
                .phoneNumber("1234567890")
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set name")
        void testNameGetterSetter() {
            userForm.setName("Jane Doe");
            assertEquals("Jane Doe", userForm.getName());
        }

        @Test
        @DisplayName("Should get and set email")
        void testEmailGetterSetter() {
            userForm.setEmail("jane@example.com");
            assertEquals("jane@example.com", userForm.getEmail());
        }

        @Test
        @DisplayName("Should get and set password")
        void testPasswordGetterSetter() {
            userForm.setPassword("newPassword");
            assertEquals("newPassword", userForm.getPassword());
        }

        @Test
        @DisplayName("Should get and set about")
        void testAboutGetterSetter() {
            userForm.setAbout("Product Manager");
            assertEquals("Product Manager", userForm.getAbout());
        }

        @Test
        @DisplayName("Should get and set phoneNumber")
        void testPhoneNumberGetterSetter() {
            userForm.setPhoneNumber("9876543210");
            assertEquals("9876543210", userForm.getPhoneNumber());
        }
    }
}

@DisplayName("ProfileForm Tests")
class ProfileFormTest {

    private ProfileForm profileForm;

    @BeforeEach
    void setUp() {
        profileForm = ProfileForm.builder()
                .name("John Doe")
                .currentPassword("oldPass123")
                .phoneNumber("1234567890")
                .about("Developer")
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set name")
        void testFirstNameGetterSetter() {
            profileForm.setName("Jane Doe");
            assertEquals("Jane Doe", profileForm.getName());
        }

        @Test
        @DisplayName("Should get and set currentPassword")
        void testLastNameGetterSetter() {
            profileForm.setCurrentPassword("newPass456");
            assertEquals("newPass456", profileForm.getCurrentPassword());
        }

        @Test
        @DisplayName("Should get and set phoneNumber")
        void testEmailGetterSetter() {
            profileForm.setPhoneNumber("9876543210");
            assertEquals("9876543210", profileForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should get and set phoneNumber alternative")
        void testPhoneNumberGetterSetter() {
            profileForm.setPhoneNumber("5555555555");
            assertEquals("5555555555", profileForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should get and set about")
        void testAboutGetterSetter() {
            profileForm.setAbout("Manager");
            assertEquals("Manager", profileForm.getAbout());
        }
    }
}

@DisplayName("ContactsSearchForm Tests")
class ContactsSearchFormTest {

    private ContactsSearchForm searchForm;

    @BeforeEach
    void setUp() {
        searchForm = ContactsSearchForm.builder()
                .keyword("John")
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set keyword")
        void testKeywordGetterSetter() {
            searchForm.setKeyword("Jane");
            assertEquals("Jane", searchForm.getKeyword());
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {
        
        @Test
        @DisplayName("Should search by name")
        void testSearchByName() {
            searchForm.setKeyword("John");
            assertEquals("John", searchForm.getKeyword());
        }

        @Test
        @DisplayName("Should search by email")
        void testSearchByEmail() {
            searchForm.setKeyword("john@example.com");
            assertEquals("john@example.com", searchForm.getKeyword());
        }

        @Test
        @DisplayName("Should handle empty search")
        void testEmptySearch() {
            searchForm.setKeyword("");
            assertEquals("", searchForm.getKeyword());
        }

        @Test
        @DisplayName("Should handle null search")
        void testNullSearch() {
            searchForm.setKeyword(null);
            assertNull(searchForm.getKeyword());
        }
    }
}

@DisplayName("ChangePasswordForm Tests")
class ChangePasswordFormTest {

    private ChangePasswordForm changePasswordForm;

    @BeforeEach
    void setUp() {
        changePasswordForm = ChangePasswordForm.builder()
                .currentPassword("oldPass123")
                .newPassword("newPass123")
                .confirmPassword("newPass123")
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set currentPassword")
        void testOldPasswordGetterSetter() {
            changePasswordForm.setCurrentPassword("differentPassword");
            assertEquals("differentPassword", changePasswordForm.getCurrentPassword());
        }

        @Test
        @DisplayName("Should get and set newPassword")
        void testNewPasswordGetterSetter() {
            changePasswordForm.setNewPassword("anotherPassword");
            assertEquals("anotherPassword", changePasswordForm.getNewPassword());
        }

        @Test
        @DisplayName("Should get and set confirmPassword")
        void testConfirmPasswordGetterSetter() {
            changePasswordForm.setConfirmPassword("anotherPassword");
            assertEquals("anotherPassword", changePasswordForm.getConfirmPassword());
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {
        
        @Test
        @DisplayName("Should match new and confirm passwords")
        void testMatchingPasswords() {
            assertEquals(changePasswordForm.getNewPassword(), changePasswordForm.getConfirmPassword());
        }

        @Test
        @DisplayName("Should detect mismatched passwords")
        void testMismatchedPasswords() {
            changePasswordForm.setConfirmPassword("differentPassword");
            assertNotEquals(changePasswordForm.getNewPassword(), changePasswordForm.getConfirmPassword());
        }
    }
}

@DisplayName("ResetPasswordForm Tests")
class ResetPasswordFormTest {

    private ResetPasswordForm resetPasswordForm;

    @BeforeEach
    void setUp() {
        resetPasswordForm = ResetPasswordForm.builder()
                .password("newPassword123")
                .confirmPassword("newPassword123")
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set password")
        void testPasswordGetterSetter() {
            resetPasswordForm.setPassword("anotherPassword");
            assertEquals("anotherPassword", resetPasswordForm.getPassword());
        }

        @Test
        @DisplayName("Should get and set confirmPassword")
        void testConfirmPasswordGetterSetter() {
            resetPasswordForm.setConfirmPassword("anotherPassword");
            assertEquals("anotherPassword", resetPasswordForm.getConfirmPassword());
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {
        
        @Test
        @DisplayName("Should validate matching passwords")
        void testMatchingPasswords() {
            assertEquals(resetPasswordForm.getPassword(), resetPasswordForm.getConfirmPassword());
        }

        @Test
        @DisplayName("Should detect mismatched passwords")
        void testMismatchedPasswords() {
            resetPasswordForm.setConfirmPassword("differentPassword");
            assertNotEquals(resetPasswordForm.getPassword(), resetPasswordForm.getConfirmPassword());
        }
    }
}

@DisplayName("FeedbackForm Tests")
class FeedbackFormTest {

    private FeedbackForm feedbackForm;

    @BeforeEach
    void setUp() {
        feedbackForm = new FeedbackForm();
        feedbackForm.setName("John Doe");
        feedbackForm.setEmail("john@example.com");
        feedbackForm.setMessage("Please add dark mode support");
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set name")
        void testNameGetterSetter() {
            feedbackForm.setName("Jane Doe");
            assertEquals("Jane Doe", feedbackForm.getName());
        }

        @Test
        @DisplayName("Should get and set email")
        void testEmailGetterSetter() {
            feedbackForm.setEmail("jane@example.com");
            assertEquals("jane@example.com", feedbackForm.getEmail());
        }

        @Test
        @DisplayName("Should get and set message")
        void testSubjectGetterSetter() {
            feedbackForm.setMessage("Bug Report");
            assertEquals("Bug Report", feedbackForm.getMessage());
        }

        @Test
        @DisplayName("Should get and set message alternative")
        void testMessageGetterSetter() {
            feedbackForm.setMessage("There is a bug in the search feature");
            assertEquals("There is a bug in the search feature", feedbackForm.getMessage());
        }
    }

    @Nested
    @DisplayName("Feedback Type Tests")
    class FeedbackTypeTests {
        
        @Test
        @DisplayName("Should support Feature Request feedback")
        void testFeatureRequestFeedback() {
            feedbackForm.setMessage("Feature Request");
            assertEquals("Feature Request", feedbackForm.getMessage());
        }

        @Test
        @DisplayName("Should support Bug Report feedback")
        void testBugReportFeedback() {
            feedbackForm.setMessage("Bug Report");
            assertEquals("Bug Report", feedbackForm.getMessage());
        }

        @Test
        @DisplayName("Should support General feedback")
        void testGeneralFeedback() {
            feedbackForm.setMessage("General");
            assertEquals("General", feedbackForm.getMessage());
        }
    }
}
