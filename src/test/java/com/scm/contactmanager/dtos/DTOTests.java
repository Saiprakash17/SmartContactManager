package com.scm.contactmanager.dtos;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create LoginRequest with Builder")
        void testBuilder() {
            assertNotNull(loginRequest);
            assertEquals("user@example.com", loginRequest.getEmail());
            assertEquals("password123", loginRequest.getPassword());
        }

        @Test
        @DisplayName("Should create LoginRequest with NoArgsConstructor")
        void testNoArgsConstructor() {
            LoginRequest request = new LoginRequest();
            assertNotNull(request);
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set email")
        void testEmailGetterSetter() {
            loginRequest.setEmail("newemail@example.com");
            assertEquals("newemail@example.com", loginRequest.getEmail());
        }

        @Test
        @DisplayName("Should get and set password")
        void testPasswordGetterSetter() {
            loginRequest.setPassword("newPassword");
            assertEquals("newPassword", loginRequest.getPassword());
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {
        
        @Test
        @DisplayName("Should accept valid email")
        void testValidEmail() {
            loginRequest.setEmail("user@example.com");
            assertEquals("user@example.com", loginRequest.getEmail());
        }

        @Test
        @DisplayName("Should accept email with numbers")
        void testEmailWithNumbers() {
            loginRequest.setEmail("user123@example.com");
            assertEquals("user123@example.com", loginRequest.getEmail());
        }
    }
}

@DisplayName("LoginResponse DTO Tests")
class LoginResponseTest {

    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginResponse = LoginResponse.builder()
                .token("jwt-token-123")
                .username("john_doe")
                .email("john@example.com")
                .expiresIn(3600L)
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create LoginResponse with Builder")
        void testBuilder() {
            assertNotNull(loginResponse);
            assertEquals("jwt-token-123", loginResponse.getToken());
            assertEquals("john_doe", loginResponse.getUsername());
            assertEquals("john@example.com", loginResponse.getEmail());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set username")
        void testUsernameGetterSetter() {
            loginResponse.setUsername("jane_doe");
            assertEquals("jane_doe", loginResponse.getUsername());
        }

        @Test
        @DisplayName("Should get and set token")
        void testTokenGetterSetter() {
            loginResponse.setToken("new-token");
            assertEquals("new-token", loginResponse.getToken());
        }

        @Test
        @DisplayName("Should get and set email")
        void testEmailGetterSetter() {
            loginResponse.setEmail("new@example.com");
            assertEquals("new@example.com", loginResponse.getEmail());
        }
    }

    @Nested
    @DisplayName("Response Status Tests")
    class ResponseStatusTests {
        
        @Test
        @DisplayName("Should include token in response")
        void testSuccessfulResponse() {
            assertNotNull(loginResponse.getToken());
            assertTrue(loginResponse.getToken().length() > 0);
        }

        @Test
        @DisplayName("Should set default type to Bearer")
        void testFailedResponse() {
            assertEquals("Bearer", loginResponse.getType());
        }
    }
}

@DisplayName("ActivityStatistics DTO Tests")
class ActivityStatisticsTest {

    private ActivityStatistics stats;

    @BeforeEach
    void setUp() {
        stats = ActivityStatistics.builder()
                .created(10)
                .updated(5)
                .deleted(2)
                .viewed(8)
                .exported(1)
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set created count")
        void testContactsCreatedGetterSetter() {
            stats.setCreated(20);
            assertEquals(20, stats.getCreated());
        }

        @Test
        @DisplayName("Should get and set updated count")
        void testContactsUpdatedGetterSetter() {
            stats.setUpdated(15);
            assertEquals(15, stats.getUpdated());
        }

        @Test
        @DisplayName("Should get and set deleted count")
        void testContactsDeletedGetterSetter() {
            stats.setDeleted(5);
            assertEquals(5, stats.getDeleted());
        }
    }

    @Nested
    @DisplayName("Statistics Calculation Tests")
    class StatisticsCalculationTests {
        
        @Test
        @DisplayName("Should calculate total operations")
        void testTotalOperations() {
            long total = stats.getCreated() + stats.getUpdated() + stats.getDeleted();
            assertTrue(total > 0);
        }

        @Test
        @DisplayName("Should handle zero statistics")
        void testZeroStatistics() {
            ActivityStatistics zeroStats = ActivityStatistics.builder()
                    .created(0)
                    .updated(0)
                    .deleted(0)
                    .build();
            
            assertEquals(0, zeroStats.getCreated());
        }
    }
}

@DisplayName("CommunicationLogResponse DTO Tests")
class CommunicationLogResponseTest {

    private CommunicationLogResponse response;

    @BeforeEach
    void setUp() {
        response = CommunicationLogResponse.builder()
                .id(1L)
                .contactId(10L)
                .contactName("John Doe")
                .type("CALL")
                .notes("Discussed arrangement")
                .outcome("Positive")
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set type")
        void testTypeGetterSetter() {
            response.setType("EMAIL");
            assertEquals("EMAIL", response.getType());
        }

        @Test
        @DisplayName("Should get and set notes")
        void testDateGetterSetter() {
            response.setNotes("New notes");
            assertEquals("New notes", response.getNotes());
        }

        @Test
        @DisplayName("Should get and set outcome")
        void testNotesGetterSetter() {
            response.setOutcome("Negative");
            assertEquals("Negative", response.getOutcome());
        }
    }
}

@DisplayName("ImportantDateResponse DTO Tests")
class ImportantDateResponseTest {

    private ImportantDateResponse response;

    @BeforeEach
    void setUp() {
        response = ImportantDateResponse.builder()
                .id(1L)
                .contactId(5L)
                .contactName("Jane Smith")
                .name("Birthday")
                .daysBeforeNotify(7)
                .notificationEnabled(true)
                .build();
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set name")
        void testTitleGetterSetter() {
            response.setName("Anniversary");
            assertEquals("Anniversary", response.getName());
        }

        @Test
        @DisplayName("Should get and set daysBeforeNotify")
        void testDateGetterSetter() {
            response.setDaysBeforeNotify(14);
            assertEquals(14, response.getDaysBeforeNotify());
        }
    }
}
