package com.scm.contactmanager.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Address Entity Tests")
class AddressTest {

    private Address address;
    private Contact contact;

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .id(1L)
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .country("USA")
                .zipCode("62701")
                .build();

        contact = Contact.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .address(address)
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create address with NoArgsConstructor")
        void testNoArgsConstructor() {
            Address newAddress = new Address();
            assertNotNull(newAddress);
            assertEquals(0, newAddress.getId());
            assertNull(newAddress.getStreet());
        }

        @Test
        @DisplayName("Should create address with Builder")
        void testBuilder() {
            assertNotNull(address);
            assertEquals(1L, address.getId());
            assertEquals("123 Main St", address.getStreet());
            assertEquals("Springfield", address.getCity());
        }

        @Test
        @DisplayName("Should create address with AllArgsConstructor")
        void testAllArgsConstructor() {
            Address newAddress = Address.builder()
                    .id(1L)
                    .street("456 Oak St")
                    .city("Chicago")
                    .state("IL")
                    .country("USA")
                    .zipCode("60601")
                    .build();
            assertEquals(1L, newAddress.getId());
            assertEquals("456 Oak St", newAddress.getStreet());
            assertEquals("Chicago", newAddress.getCity());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            address.setId(999L);
            assertEquals(999L, address.getId());
        }

        @Test
        @DisplayName("Should get and set street")
        void testStreetGetterSetter() {
            address.setStreet("456 New St");
            assertEquals("456 New St", address.getStreet());
        }

        @Test
        @DisplayName("Should get and set city")
        void testCityGetterSetter() {
            address.setCity("Chicago");
            assertEquals("Chicago", address.getCity());
        }

        @Test
        @DisplayName("Should get and set state")
        void testStateGetterSetter() {
            address.setState("CA");
            assertEquals("CA", address.getState());
        }

        @Test
        @DisplayName("Should get and set country")
        void testCountryGetterSetter() {
            address.setCountry("Canada");
            assertEquals("Canada", address.getCountry());
        }

        @Test
        @DisplayName("Should get and set postal code")
        void testzipCodeGetterSetter() {
            address.setZipCode("90210");
            assertEquals("90210", address.getZipCode());
        }
    }

    @Nested
    @DisplayName("Address Details Tests")
    class AddressDetailsTests {
        
        @Test
        @DisplayName("Should handle complete US address")
        void testCompleteUSAddress() {
            assertEquals("123 Main St", address.getStreet());
            assertEquals("Springfield", address.getCity());
            assertEquals("IL", address.getState());
            assertEquals("USA", address.getCountry());
            assertEquals("62701", address.getZipCode());
        }

        @Test
        @DisplayName("Should handle international address")
        void testInternationalAddress() {
            address.setCountry("United Kingdom");
            address.setZipCode("SW1A 1AA");
            assertEquals("United Kingdom", address.getCountry());
            assertEquals("SW1A 1AA", address.getZipCode());
        }

        @Test
        @DisplayName("Should handle address with apartment")
        void testAddressWithApartment() {
            address.setStreet("123 Main St, Apt 4B");
            assertEquals("123 Main St, Apt 4B", address.getStreet());
        }

        @Test
        @DisplayName("Should handle address changes")
        void testAddressChanges() {
            address.setStreet("789 Pine St");
            address.setCity("Boston");
            address.setState("MA");
            address.setZipCode("02101");
            
            assertEquals("789 Pine St", address.getStreet());
            assertEquals("Boston", address.getCity());
            assertEquals("MA", address.getState());
            assertEquals("02101", address.getZipCode());
        }
    }

    @Nested
    @DisplayName("Null & Empty Tests")
    class NullAndEmptyTests {
        
        @Test
        @DisplayName("Should handle null street")
        void testNullStreet() {
            address.setStreet(null);
            assertNull(address.getStreet());
        }

        @Test
        @DisplayName("Should handle empty string values")
        void testEmptyValues() {
            address.setStreet("");
            address.setCity("");
            assertEquals("", address.getStreet());
            assertEquals("", address.getCity());
        }

        @Test
        @DisplayName("Should handle null postal code")
        void testNullzipCode() {
            address.setZipCode(null);
            assertNull(address.getZipCode());
        }
    }

    @Nested
    @DisplayName("Minimal Address Tests")
    class MinimalAddressTests {
        
        @Test
        @DisplayName("Should create minimal address")
        void testMinimalAddress() {
            Address minimal = Address.builder().build();
            assertNotNull(minimal);
            assertNull(minimal.getStreet());
            assertNull(minimal.getCity());
        }

        @Test
        @DisplayName("Should create address with only street")
        void testAddressWithOnlyStreet() {
            Address minAddress = Address.builder()
                    .street("123 Main St")
                    .build();
            
            assertEquals("123 Main St", minAddress.getStreet());
            assertNull(minAddress.getCity());
        }
    }

    @Nested
    @DisplayName("Relationship Tests")
    class RelationshipTests {
        
        @Test
        @DisplayName("Should have relationship with Contact (OneToOne)")
        void testContactRelationship() {
            // Address is referenced by Contact via OneToOne
            assertNotNull(contact);
            assertEquals(address.getId(), contact.getAddress().getId());
        }

        @Test
        @DisplayName("Should update address for contact")
        void testUpdateAddressForContact() {
            Address newAddress = Address.builder()
                    .id(2L)
                    .street("456 Oak St")
                    .city("New City")
                    .state("CA")
                    .country("USA")
                    .zipCode("90210")
                    .build();
            
            contact.setAddress(newAddress);
            assertEquals(newAddress.getId(), contact.getAddress().getId());
            assertEquals("456 Oak St", contact.getAddress().getStreet());
        }
    }

    @Nested
    @DisplayName("Address Validation Pattern Tests")
    class AddressValidationPatternTests {
        
        @Test
        @DisplayName("Should accept 5-digit postal code")
        void testFiveDigitzipCode() {
            address.setZipCode("62701");
            assertEquals("62701", address.getZipCode());
        }

        @Test
        @DisplayName("Should accept 9-digit postal code")
        void testNineDigitzipCode() {
            address.setZipCode("627011234");
            assertEquals("627011234", address.getZipCode());
        }

        @Test
        @DisplayName("Should accept postal code with hyphen (US ZIP+4)")
        void testzipCodeWithHyphen() {
            address.setZipCode("62701-1234");
            assertEquals("62701-1234", address.getZipCode());
        }

        @Test
        @DisplayName("Should accept UK postcode format")
        void testUKPostcodeFormat() {
            address.setCountry("United Kingdom");
            address.setZipCode("SW1A 1AA");
            assertEquals("SW1A 1AA", address.getZipCode());
        }

        @Test
        @DisplayName("Should accept Canadian postal code")
        void testCanadianzipCode() {
            address.setCountry("Canada");
            address.setZipCode("K1A 0B1");
            assertEquals("K1A 0B1", address.getZipCode());
        }
    }

    @Nested
    @DisplayName("State Code & Full Name Tests")
    class StateTests {
        
        @Test
        @DisplayName("Should accept US state abbreviations")
        void testUSStateAbbreviations() {
            String[] states = {"CA", "TX", "FL", "NY", "IL", "OH"};
            
            for (String state : states) {
                address.setState(state);
                assertEquals(state, address.getState());
            }
        }

        @Test
        @DisplayName("Should accept full state names")
        void testFullStateNames() {
            address.setState("California");
            assertEquals("California", address.getState());
            
            address.setState("Texas");
            assertEquals("Texas", address.getState());
        }

        @Test
        @DisplayName("Should accept international provinces")
        void testInternationalProvinces() {
            address.setState("Ontario");
            assertEquals("Ontario", address.getState());
            
            address.setState("British Columbia");
            assertEquals("British Columbia", address.getState());
        }
    }

    @Nested
    @DisplayName("Complex Address Scenarios Tests")
    class ComplexAddressScenarioTests {
        
        @Test
        @DisplayName("Should handle address relocation")
        void testAddressRelocation() {
            // Original address
            assertEquals("Springfield", address.getCity());
            
            // Relocate
            address.setStreet("999 New Location St");
            address.setCity("Los Angeles");
            address.setState("CA");
            address.setZipCode("90210");
            
            assertEquals("Los Angeles", address.getCity());
            assertEquals("CA", address.getState());
        }

        @Test
        @DisplayName("Should create new address and maintain old reference")
        void testCreateNewAddressWhileMaintainingOld() {
            long originalId = address.getId();
            
            Address newAddress = Address.builder()
                    .id(2L)
                    .street("New Street")
                    .city("New City")
                    .state("CA")
                    .country("USA")
                    .zipCode("90210")
                    .build();
            
            assertEquals(originalId, address.getId());
            assertEquals(2L, newAddress.getId());
            assertNotEquals(address.getId(), newAddress.getId());
        }

        @Test
        @DisplayName("Should handle address with long street name")
        void testLongStreetName() {
            String longStreet = "1234 " + "Very ".repeat(20) + "Long Street Name";
            address.setStreet(longStreet);
            assertTrue(address.getStreet().length() > 50);
        }
    }
}
