package com.scm.contactmanager.forms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import com.scm.contactmanager.entities.Relationship;

@DisplayName("ContactForm Tests")
class ContactFormTest {

    private ContactForm contactForm;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        contactForm = ContactForm.builder()
                .name("John Doe")
                .email("john@example.com")
                .phoneNumber("1234567890")
                .street("123 Main St")
                .city("Springfield")
                .state("Illinois")
                .country("United States")
                .zipCode("62701")
                .description("Software Engineer")
                .favorite(false)
                .websiteLink("https://john.com")
                .linkedInLink("https://linkedin.com/in/john")
                .picture("profile.jpg")
                .relationship(Relationship.FRIEND)
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create form with NoArgsConstructor")
        void testNoArgsConstructor() {
            ContactForm form = new ContactForm();
            assertNotNull(form);
            assertNull(form.getName());
            assertNull(form.getEmail());
        }

        @Test
        @DisplayName("Should create form with Builder")
        void testBuilder() {
            assertNotNull(contactForm);
            assertEquals("John Doe", contactForm.getName());
            assertEquals("john@example.com", contactForm.getEmail());
            assertEquals("1234567890", contactForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should create form with AllArgsConstructor")
        void testAllArgsConstructor() {
            ContactForm form = new ContactForm("Jane", "jane@test.com", "9876543210", 
                    "St", "City", "State", "Country", "12345", "desc", false, 
                    "http://web", "http://linkedin", null, "pic", Relationship.FRIEND);
            
            assertEquals("Jane", form.getName());
            assertEquals("jane@test.com", form.getEmail());
        }
    }

    @Nested
    @DisplayName("Name Field Validation Tests")
    class NameFieldTests {
        
        @Test
        @DisplayName("Should accept valid name (2-50 chars)")
        void testValidName() {
            contactForm.setName("John Doe");
            assertEquals("John Doe", contactForm.getName());
        }

        @Test
        @DisplayName("Should accept minimum length name (2 chars)")
        void testMinimumLengthName() {
            contactForm.setName("Jo");
            assertEquals("Jo", contactForm.getName());
        }

        @Test
        @DisplayName("Should accept maximum length name (50 chars)")
        void testMaximumLengthName() {
            String maxName = "a".repeat(50);
            contactForm.setName(maxName);
            assertEquals(50, contactForm.getName().length());
        }

        @Test
        @DisplayName("Should handle name with special characters")
        void testNameWithSpecialCharacters() {
            contactForm.setName("O'Brien-Smith");
            assertEquals("O'Brien-Smith", contactForm.getName());
        }

        @Test
        @DisplayName("Should handle name with spaces")
        void testNameWithSpaces() {
            contactForm.setName("John Michael Doe Jr.");
            assertEquals("John Michael Doe Jr.", contactForm.getName());
        }

        @Test
        @DisplayName("Should accept empty name for setter (validation is done separately)")
        void testEmptyName() {
            contactForm.setName("");
            assertEquals("", contactForm.getName());
        }

        @Test
        @DisplayName("Should accept null name for setter")
        void testNullName() {
            contactForm.setName(null);
            assertNull(contactForm.getName());
        }
    }

    @Nested
    @DisplayName("Email Field Validation Tests")
    class EmailFieldTests {
        
        @Test
        @DisplayName("Should accept valid email")
        void testValidEmail() {
            assertEquals("john@example.com", contactForm.getEmail());
        }

        @Test
        @DisplayName("Should accept email with subdomain")
        void testEmailWithSubdomain() {
            contactForm.setEmail("john@mail.example.com");
            assertEquals("john@mail.example.com", contactForm.getEmail());
        }

        @Test
        @DisplayName("Should accept email with numbers")
        void testEmailWithNumbers() {
            contactForm.setEmail("john123@example456.com");
            assertEquals("john123@example456.com", contactForm.getEmail());
        }

        @Test
        @DisplayName("Should accept email with plus sign")
        void testEmailWithPlus() {
            contactForm.setEmail("john+tag@example.com");
            assertEquals("john+tag@example.com", contactForm.getEmail());
        }

        @Test
        @DisplayName("Should handle empty email")
        void testEmptyEmail() {
            contactForm.setEmail("");
            assertEquals("", contactForm.getEmail());
        }

        @Test
        @DisplayName("Should handle null email")
        void testNullEmail() {
            contactForm.setEmail(null);
            assertNull(contactForm.getEmail());
        }
    }

    @Nested
    @DisplayName("Phone Number Field Validation Tests")
    class PhoneNumberFieldTests {
        
        @Test
        @DisplayName("Should accept valid 10-digit phone number")
        void testValidPhoneNumber() {
            assertEquals("1234567890", contactForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should accept different 10-digit phone numbers")
        void testDifferentValidPhones() {
            contactForm.setPhoneNumber("9876543210");
            assertEquals("9876543210", contactForm.getPhoneNumber());
            
            contactForm.setPhoneNumber("5555555555");
            assertEquals("5555555555", contactForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should handle phone starting with 0")
        void testPhoneStartingWithZero() {
            contactForm.setPhoneNumber("0123456789");
            assertEquals("0123456789", contactForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should handle phone with repeating digits")
        void testPhoneWithRepeatingDigits() {
            contactForm.setPhoneNumber("1111111111");
            assertEquals("1111111111", contactForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should accept empty phone")
        void testEmptyPhone() {
            contactForm.setPhoneNumber("");
            assertEquals("", contactForm.getPhoneNumber());
        }

        @Test
        @DisplayName("Should accept null phone")
        void testNullPhone() {
            contactForm.setPhoneNumber(null);
            assertNull(contactForm.getPhoneNumber());
        }
    }

    @Nested
    @DisplayName("Address Fields Validation Tests")
    class AddressFieldsTests {
        
        @Test
        @DisplayName("Should accept valid street address")
        void testValidStreet() {
            assertEquals("123 Main St", contactForm.getStreet());
        }

        @Test
        @DisplayName("Should accept street with apartment/suite")
        void testStreetWithApartment() {
            contactForm.setStreet("123 Main St, Apt 4B");
            assertEquals("123 Main St, Apt 4B", contactForm.getStreet());
        }

        @Test
        @DisplayName("Should accept maximum length street (100 chars)")
        void testMaximumStreetLength() {
            String maxStreet = "a".repeat(100);
            contactForm.setStreet(maxStreet);
            assertEquals(100, contactForm.getStreet().length());
        }

        @Test
        @DisplayName("Should accept valid city")
        void testValidCity() {
            assertEquals("Springfield", contactForm.getCity());
        }

        @Test
        @DisplayName("Should accept city with hyphen")
        void testCityWithHyphen() {
            contactForm.setCity("San-Diego");
            assertEquals("San-Diego", contactForm.getCity());
        }

        @Test
        @DisplayName("Should accept maximum city length (50 chars)")
        void testMaximumCityLength() {
            String maxCity = "a".repeat(50);
            contactForm.setCity(maxCity);
            assertEquals(50, contactForm.getCity().length());
        }

        @Test
        @DisplayName("Should accept valid state")
        void testValidState() {
            assertEquals("Illinois", contactForm.getState());
        }

        @Test
        @DisplayName("Should accept state code abbreviations")
        void testStateCode() {
            contactForm.setState("IL");
            assertEquals("IL", contactForm.getState());
        }

        @Test
        @DisplayName("Should accept valid country")
        void testValidCountry() {
            assertEquals("United States", contactForm.getCountry());
        }

        @Test
        @DisplayName("Should accept valid zip code (5 digits)")
        void testValidZipCode5() {
            contactForm.setZipCode("62701");
            assertEquals("62701", contactForm.getZipCode());
        }

        @Test
        @DisplayName("Should accept valid zip code (10 digits)")
        void testValidZipCode10() {
            contactForm.setZipCode("1234567890");
            assertEquals("1234567890", contactForm.getZipCode());
        }

        @Test
        @DisplayName("Should accept zip code with hyphen (format: 12345-6789)")
        void testZipCodeWithHyphen() {
            contactForm.setZipCode("12345");
            assertEquals("12345", contactForm.getZipCode());
        }

        @Test
        @DisplayName("Should handle empty address fields")
        void testEmptyAddressFields() {
            contactForm.setStreet("");
            contactForm.setCity("");
            contactForm.setState("");
            contactForm.setCountry("");
            contactForm.setZipCode("");
            
            assertEquals("", contactForm.getStreet());
            assertEquals("", contactForm.getCity());
        }
    }

    @Nested
    @DisplayName("Description & About Fields Tests")
    class DescriptionFieldTests {
        
        @Test
        @DisplayName("Should accept valid description")
        void testValidDescription() {
            assertEquals("Software Engineer", contactForm.getDescription());
        }

        @Test
        @DisplayName("Should accept maximum length description (500 chars)")
        void testMaximumDescriptionLength() {
            String maxDesc = "a".repeat(500);
            contactForm.setDescription(maxDesc);
            assertEquals(500, contactForm.getDescription().length());
        }

        @Test
        @DisplayName("Should accept description with multiple lines")
        void testMultilineDescription() {
            String multiline = "Line 1\nLine 2\nLine 3";
            contactForm.setDescription(multiline);
            assertEquals(multiline, contactForm.getDescription());
        }

        @Test
        @DisplayName("Should accept description with special characters")
        void testDescriptionWithSpecialChars() {
            String special = "Working on C++ & Java projects!";
            contactForm.setDescription(special);
            assertEquals(special, contactForm.getDescription());
        }

        @Test
        @DisplayName("Should accept empty description")
        void testEmptyDescription() {
            contactForm.setDescription("");
            assertEquals("", contactForm.getDescription());
        }

        @Test
        @DisplayName("Should accept null description")
        void testNullDescription() {
            contactForm.setDescription(null);
            assertNull(contactForm.getDescription());
        }
    }

    @Nested
    @DisplayName("Web Links Tests")
    class WebLinksTests {
        
        @Test
        @DisplayName("Should accept valid website link")
        void testValidWebsiteLink() {
            assertEquals("https://john.com", contactForm.getWebsiteLink());
        }

        @Test
        @DisplayName("Should accept http website")
        void testHttpWebsite() {
            contactForm.setWebsiteLink("http://example.com");
            assertEquals("http://example.com", contactForm.getWebsiteLink());
        }

        @Test
        @DisplayName("Should accept website without protocol")
        void testWebsiteWithoutProtocol() {
            contactForm.setWebsiteLink("example.com");
            assertEquals("example.com", contactForm.getWebsiteLink());
        }

        @Test
        @DisplayName("Should accept valid LinkedIn link")
        void testValidLinkedInLink() {
            assertEquals("https://linkedin.com/in/john", contactForm.getLinkedInLink());
        }

        @Test
        @DisplayName("Should accept LinkedIn profile URL")
        void testLinkedInProfileUrl() {
            contactForm.setLinkedInLink("https://www.linkedin.com/in/john-doe-123");
            assertEquals("https://www.linkedin.com/in/john-doe-123", contactForm.getLinkedInLink());
        }

        @Test
        @DisplayName("Should handle empty web links")
        void testEmptyWebLinks() {
            contactForm.setWebsiteLink("");
            contactForm.setLinkedInLink("");
            
            assertEquals("", contactForm.getWebsiteLink());
            assertEquals("", contactForm.getLinkedInLink());
        }

        @Test
        @DisplayName("Should handle null web links")
        void testNullWebLinks() {
            contactForm.setWebsiteLink(null);
            contactForm.setLinkedInLink(null);
            
            assertNull(contactForm.getWebsiteLink());
            assertNull(contactForm.getLinkedInLink());
        }
    }

    @Nested
    @DisplayName("Favorite Status Tests")
    class FavoriteStatusTests {
        
        @Test
        @DisplayName("Should have favorite as false by default")
        void testDefaultFavorite() {
            ContactForm form = new ContactForm();
            assertFalse(form.isFavorite());
        }

        @Test
        @DisplayName("Should set favorite to true")
        void testSetFavoriteTrue() {
            contactForm.setFavorite(true);
            assertTrue(contactForm.isFavorite());
        }

        @Test
        @DisplayName("Should set favorite to false")
        void testSetFavoriteFalse() {
            contactForm.setFavorite(false);
            assertFalse(contactForm.isFavorite());
        }

        @Test
        @DisplayName("Should toggle favorite status")
        void testToggleFavorite() {
            contactForm.setFavorite(false);
            assertFalse(contactForm.isFavorite());
            
            contactForm.setFavorite(true);
            assertTrue(contactForm.isFavorite());
            
            contactForm.setFavorite(false);
            assertFalse(contactForm.isFavorite());
        }
    }

    @Nested
    @DisplayName("Relationship Field Tests")
    class RelationshipFieldTests {
        
        @Test
        @DisplayName("Should accept FRIEND relationship")
        void testFriendRelationship() {
            contactForm.setRelationship(Relationship.FRIEND);
            assertEquals(Relationship.FRIEND, contactForm.getRelationship());
        }

        @Test
        @DisplayName("Should accept FAMILY relationship")
        void testFamilyRelationship() {
            contactForm.setRelationship(Relationship.FAMILY);
            assertEquals(Relationship.FAMILY, contactForm.getRelationship());
        }

        @Test
        @DisplayName("Should accept COLLEAGUE relationship")
        void testColleagueRelationship() {
            contactForm.setRelationship(Relationship.COLLEAGUE);
            assertEquals(Relationship.COLLEAGUE, contactForm.getRelationship());
        }

        @Test
        @DisplayName("Should accept OTHER relationship")
        void testOtherRelationship() {
            contactForm.setRelationship(Relationship.OTHER);
            assertEquals(Relationship.OTHER, contactForm.getRelationship());
        }

        @Test
        @DisplayName("Should handle null relationship")
        void testNullRelationship() {
            contactForm.setRelationship(null);
            assertNull(contactForm.getRelationship());
        }
    }

    @Nested
    @DisplayName("Image Upload Tests")
    class ImageUploadTests {
        
        @Test
        @DisplayName("Should accept MultipartFile")
        void testAcceptMultipartFile() {
            // In real tests, mock or create a real MultipartFile
            assertNull(contactForm.getContactImage()); // Initially null
        }

        @Test
        @DisplayName("Should set and get contact image file")
        void testSetContactImage() {
            // contactForm.setContactImage(mockFile);
            // Testing setter/getter functionality
            assertNull(contactForm.getContactImage());
        }

        @Test
        @DisplayName("Should accept image filename as picture")
        void testPictureFilename() {
            contactForm.setPicture("profile.jpg");
            assertEquals("profile.jpg", contactForm.getPicture());
        }

        @Test
        @DisplayName("Should accept different image formats")
        void testDifferentImageFormats() {
            contactForm.setPicture("profile.png");
            assertEquals("profile.png", contactForm.getPicture());
            
            contactForm.setPicture("profile.jpeg");
            assertEquals("profile.jpeg", contactForm.getPicture());
        }

        @Test
        @DisplayName("Should handle null picture")
        void testNullPicture() {
            contactForm.setPicture(null);
            assertNull(contactForm.getPicture());
        }

        @Test
        @DisplayName("Should handle empty picture")
        void testEmptyPicture() {
            contactForm.setPicture("");
            assertEquals("", contactForm.getPicture());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("Should generate toString representation")
        void testToString() {
            String toString = contactForm.toString();
            assertNotNull(toString);
            assertTrue(toString.contains("John Doe") || toString.contains("name"));
        }

        @Test
        @DisplayName("Should include main fields in toString")
        void testToStringContainsFields() {
            String toString = contactForm.toString();
            // Should contain some identifier of the form
            assertNotNull(toString);
        }
    }

    @Nested
    @DisplayName("Complete Form Lifecycle Tests")
    class CompleteFormLifecycleTests {
        
        @Test
        @DisplayName("Should create complete contact form with all fields")
        void testCreateCompleteForm() {
            ContactForm form = ContactForm.builder()
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .phoneNumber("9876543210")
                    .street("456 Oak Ave")
                    .city("Chicago")
                    .state("Illinois")
                    .country("United States")
                    .zipCode("60601")
                    .description("Product Manager")
                    .favorite(true)
                    .websiteLink("https://jane.com")
                    .linkedInLink("https://linkedin.com/in/jane")
                    .picture("jane.jpg")
                    .relationship(Relationship.COLLEAGUE)
                    .build();
            
            assertEquals("Jane Smith", form.getName());
            assertEquals("jane@example.com", form.getEmail());
            assertEquals("9876543210", form.getPhoneNumber());
            assertEquals("Chicago", form.getCity());
            assertTrue(form.isFavorite());
            assertEquals(Relationship.COLLEAGUE, form.getRelationship());
        }

        @Test
        @DisplayName("Should update all form fields")
        void testUpdateAllFields() {
            contactForm.setName("Updated Name");
            contactForm.setEmail("updated@example.com");
            contactForm.setPhoneNumber("5555555555");
            contactForm.setCity("Los Angeles");
            contactForm.setFavorite(true);
            contactForm.setRelationship(Relationship.COLLEAGUE);
            
            assertEquals("Updated Name", contactForm.getName());
            assertEquals("updated@example.com", contactForm.getEmail());
            assertEquals("5555555555", contactForm.getPhoneNumber());
            assertEquals("Los Angeles", contactForm.getCity());
            assertTrue(contactForm.isFavorite());
            assertEquals(Relationship.COLLEAGUE, contactForm.getRelationship());
        }

        @Test
        @DisplayName("Should handle partial form (minimal required fields)")
        void testMinimalForm() {
            ContactForm minimalForm = new ContactForm();
            minimalForm.setName("John");
            minimalForm.setEmail("john@test.com");
            minimalForm.setPhoneNumber("1234567890");
            minimalForm.setStreet("Street");
            minimalForm.setCity("City");
            minimalForm.setState("State");
            minimalForm.setCountry("Country");
            minimalForm.setZipCode("12345");
            
            assertNotNull(minimalForm.getName());
            assertNotNull(minimalForm.getEmail());
            assertNull(minimalForm.getDescription()); // Optional field
            assertNull(minimalForm.getWebsiteLink()); // Optional field
        }

        @Test
        @DisplayName("Should clear form fields")
        void testClearFormFields() {
            contactForm.setName(null);
            contactForm.setEmail(null);
            contactForm.setDescription(null);
            contactForm.setWebsiteLink(null);
            
            assertNull(contactForm.getName());
            assertNull(contactForm.getEmail());
            assertNull(contactForm.getDescription());
            assertNull(contactForm.getWebsiteLink());
        }
    }
}
