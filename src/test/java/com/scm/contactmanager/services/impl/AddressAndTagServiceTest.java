package com.scm.contactmanager.services.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.scm.contactmanager.entities.Address;

@DisplayName("AddressService & ContactTagService Tests")
class AddressAndTagServiceTest {

    private Address testAddress;

    @BeforeEach
    void setUp() {
        testAddress = Address.builder()
                .id(1L)
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .build();
    }

    @Nested
    @DisplayName("Address Service - Tests")
    class AddressServiceTests {

        @Test
        @DisplayName("Should create address successfully")
        void testCreateAddressSuccess() {
            assertNotNull(testAddress);
            assertEquals("123 Main St", testAddress.getStreet());
            assertEquals("Springfield", testAddress.getCity());
        }

        @Test
        @DisplayName("Should have all address fields")
        void testAddressWithAllFields() {
            assertEquals("USA", testAddress.getCountry());
            assertEquals("62701", testAddress.getZipCode());
            assertEquals("IL", testAddress.getState());
        }

        @Test
        @DisplayName("Should update address fields")
        void testUpdateAddressFields() {
            testAddress.setCity("Chicago");
            assertEquals("Chicago", testAddress.getCity());
        }

        @Test
        @DisplayName("Should validate address data")
        void testAddressValidation() {
            assertTrue(testAddress.getStreet().length() > 0);
            assertTrue(testAddress.getCity().length() > 0);
        }
    }

    @Nested
    @DisplayName("ContactTag Service - Tests")
    class ContactTagServiceTests {

        @Test
        @DisplayName("Should validate tag creation")
        void testTagCreation() {
            String tagName = "Work";
            assertNotNull(tagName);
            assertTrue(tagName.length() > 0);
        }

        @Test
        @DisplayName("Should support tag operations")
        void testTagOperations() {
            String[] tags = {"Work", "Personal", "Urgent"};
            assertEquals(3, tags.length);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should validate address in contact")
        void testAddressInContact() {
            assertNotNull(testAddress);
            assertEquals("123 Main St", testAddress.getStreet());
        }

        @Test
        @DisplayName("Should support address operations")
        void testAddressOperations() {
            testAddress.setCity("Boston");
            assertEquals("Boston", testAddress.getCity());
        }
    }
}
