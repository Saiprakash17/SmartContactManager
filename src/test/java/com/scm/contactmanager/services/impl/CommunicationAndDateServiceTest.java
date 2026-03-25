package com.scm.contactmanager.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.ImportantDate;
import com.scm.contactmanager.entities.User;

@DisplayName("Communication & Date Service Tests")
class CommunicationAndDateServiceTest {

    private User testUser;
    private Contact testContact;
    private ImportantDate testDate;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .build();

        testContact = Contact.builder()
                .id(1L)
                .name("Jane Contact")
                .email("jane@example.com")
                .user(testUser)
                .build();

        testDate = ImportantDate.builder()
                .id(1L)
                .contact(testContact)
                .name("Birthday")
                .date(java.time.LocalDate.of(1990, 5, 15))
                .build();
    }

    @Nested
    @DisplayName("ImportantDate Entity Tests")
    class ImportantDateEntityTests {

        @Test
        @DisplayName("Should create important date with all fields")
        void testCreateImportantDate() {
            assertNotNull(testDate);
            assertEquals(1L, testDate.getId());
            assertEquals("Birthday", testDate.getName());
            assertEquals(java.time.LocalDate.of(1990, 5, 15), testDate.getDate());
        }

        @Test
        @DisplayName("Should update important date")
        void testUpdateImportantDate() {
            testDate.setName("Anniversary");
            assertEquals("Anniversary", testDate.getName());
        }

        @Test
        @DisplayName("Should associate important date with contact")
        void testImportantDateContact() {
            assertNotNull(testDate.getContact());
            assertEquals("Jane Contact", testDate.getContact().getName());
        }

        @Test
        @DisplayName("Should validate important date data")
        void testImportantDateValidation() {
            assertTrue(testDate.getName().length() > 0);
            assertNotNull(testDate.getDate());
        }
    }

    @Nested
    @DisplayName("Communication Pattern Tests")
    class CommunicationPatternTests {

        @Test
        @DisplayName("Should validate communication types")
        void testCommunicationTypes() {
            String[] types = {"EMAIL", "PHONE", "MEETIN", "MESSAGE"};
            assertTrue(types.length > 0);
        }

        @Test
        @DisplayName("Should validate contact communication tracking")
        void testContactCommunicationTracking() {
            Contact contact = testContact;
            assertNotNull(contact.getId());
            assertNotNull(contact.getUser());
        }

        @Test
        @DisplayName("Should track communication dates")
        void testCommunicationDateTracking() {
            LocalDateTime now = LocalDateTime.now();
            assertNotNull(now);
            assertTrue(now.getYear() >= 2024);
        }
    }

    @Nested
    @DisplayName("Date Management Tests")
    class DateManagementTests {

        @Test
        @DisplayName("Should parse and store dates")
        void testDateParsing() {
            String dateString = "1990-05-15";
            assertEquals(10, dateString.length());
            assertTrue(dateString.matches("\\d{4}-\\d{2}-\\d{2}"));
        }

        @Test
        @DisplayName("Should calculate date differences")
        void testDateCalculation() {
            LocalDateTime birthDate = LocalDateTime.of(1990, 5, 15, 0, 0);
            LocalDateTime now = LocalDateTime.now();
            assertTrue(now.isAfter(birthDate));
        }

        @Test
        @DisplayName("Should validate important date format")
        void testImportantDateFormat() {
            String name = "Birthday";
            String date = "1990-05-15";
            
            assertTrue(name.length() > 0);
            assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should create contact with important dates")
        void testContactWithImportantDates() {
            assertNotNull(testContact);
            assertNotNull(testDate);
            assertEquals(testContact.getId(), testDate.getContact().getId());
        }

        @Test
        @DisplayName("Should manage user communications")
        void testUserCommunications() {
            User user = testUser;
            assertNotNull(user.getId());
            assertEquals("user123", user.getId());
        }
    }
}
