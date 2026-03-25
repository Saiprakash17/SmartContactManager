package com.scm.contactmanager.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Contact Entity Tests")
class ContactTest {

    private Contact contact;
    private User user;
    private Address address;
    private ContactTag tag1, tag2;
    private SocialLink socialLink;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .build();

        address = Address.builder()
                .id(1L)
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .country("USA")
                .zipCode("62701")
                .build();

        tag1 = ContactTag.builder()
                .id(1L)
                .name("Family")
                .build();

        tag2 = ContactTag.builder()
                .id(2L)
                .name("Work")
                .build();

        socialLink = SocialLink.builder()
                .id(1L)
                .title("Twitter")
                .link("https://twitter.com/example")
                .build();

        contact = Contact.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .phoneNumber("1234567890")
                .imageUrl("https://example.com/image.jpg")
                .about("Software Engineer")
                .favorite(false)
                .website("https://jane.com")
                .linkedin("https://linkedin.com/in/jane")
                .relationship(Relationship.FRIEND)
                .address(address)
                .user(user)
                .cloudinaryImagePublicId("public_id_123")
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create contact with NoArgsConstructor")
        void testNoArgsConstructor() {
            Contact newContact = new Contact();
            assertNotNull(newContact);
            assertEquals(0, newContact.getId());
            assertNull(newContact.getName());
        }

        @Test
        @DisplayName("Should create contact with Builder")
        void testBuilder() {
            assertNotNull(contact);
            assertEquals(1L, contact.getId());
            assertEquals("Jane Doe", contact.getName());
            assertEquals("jane@example.com", contact.getEmail());
        }

        @Test
        @DisplayName("Should create contact with AllArgsConstructor")
        void testAllArgsConstructor() {
            Contact newContact = new Contact(1L, "Test", "test@example.com", "123456", 
                    "img.jpg", "about", false, "website", "linkedin", 
                    Relationship.FRIEND, address, user, new HashSet<>(), new ArrayList<>(), "pid");
            
            assertEquals(1L, newContact.getId());
            assertEquals("Test", newContact.getName());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            contact.setId(999L);
            assertEquals(999L, contact.getId());
        }

        @Test
        @DisplayName("Should get and set name")
        void testNameGetterSetter() {
            contact.setName("New Name");
            assertEquals("New Name", contact.getName());
        }

        @Test
        @DisplayName("Should get and set email")
        void testEmailGetterSetter() {
            contact.setEmail("newemail@example.com");
            assertEquals("newemail@example.com", contact.getEmail());
        }

        @Test
        @DisplayName("Should get and set phoneNumber")
        void testPhoneNumberGetterSetter() {
            contact.setPhoneNumber("9876543210");
            assertEquals("9876543210", contact.getPhoneNumber());
        }

        @Test
        @DisplayName("Should get and set imageUrl")
        void testImageUrlGetterSetter() {
            contact.setImageUrl("https://new-image.jpg");
            assertEquals("https://new-image.jpg", contact.getImageUrl());
        }

        @Test
        @DisplayName("Should get and set about")
        void testAboutGetterSetter() {
            contact.setAbout("New description");
            assertEquals("New description", contact.getAbout());
        }

        @Test
        @DisplayName("Should get and set favorite")
        void testFavoriteGetterSetter() {
            contact.setFavorite(true);
            assertTrue(contact.isFavorite());
        }

        @Test
        @DisplayName("Should get and set website")
        void testWebsiteGetterSetter() {
            contact.setWebsite("https://newwebsite.com");
            assertEquals("https://newwebsite.com", contact.getWebsite());
        }

        @Test
        @DisplayName("Should get and set linkedin")
        void testLinkedinGetterSetter() {
            contact.setLinkedin("https://linkedin.com/in/newprofile");
            assertEquals("https://linkedin.com/in/newprofile", contact.getLinkedin());
        }

        @Test
        @DisplayName("Should get and set relationship")
        void testRelationshipGetterSetter() {
            contact.setRelationship(Relationship.COLLEAGUE);
            assertEquals(Relationship.COLLEAGUE, contact.getRelationship());
        }

        @Test
        @DisplayName("Should get and set address")
        void testAddressGetterSetter() {
            Address newAddress = Address.builder()
                    .id(2L)
                    .street("456 Oak St")
                    .city("Chicago")
                    .state("IL")
                    .country("USA")
                    .zipCode("60601")
                    .build();
            
            contact.setAddress(newAddress);
            assertEquals(newAddress, contact.getAddress());
            assertEquals("Chicago", contact.getAddress().getCity());
        }

        @Test
        @DisplayName("Should get and set user")
        void testUserGetterSetter() {
            User newUser = User.builder()
                    .id("user456")
                    .name("Jane Smith")
                    .email("jane@example.com")
                    .build();
            
            contact.setUser(newUser);
            assertEquals(newUser, contact.getUser());
            assertEquals("user456", contact.getUser().getId());
        }

        @Test
        @DisplayName("Should get and set cloudinaryImagePublicId")
        void testCloudinaryImagePublicIdGetterSetter() {
            contact.setCloudinaryImagePublicId("new_pid_123");
            assertEquals("new_pid_123", contact.getCloudinaryImagePublicId());
        }
    }

    @Nested
    @DisplayName("Tags Management Tests")
    class TagsManagementTests {
        
        @Test
        @DisplayName("Should have empty tags by default")
        void testDefaultEmptyTags() {
            Contact newContact = new Contact();
            assertNotNull(newContact.getTags());
            assertEquals(0, newContact.getTags().size());
        }

        @Test
        @DisplayName("Should add single tag")
        void testAddSingleTag() {
            contact.getTags().add(tag1);
            assertEquals(1, contact.getTags().size());
            assertTrue(contact.getTags().contains(tag1));
        }

        @Test
        @DisplayName("Should add multiple tags")
        void testAddMultipleTags() {
            contact.getTags().add(tag1);
            contact.getTags().add(tag2);
            
            assertEquals(2, contact.getTags().size());
            assertTrue(contact.getTags().contains(tag1));
            assertTrue(contact.getTags().contains(tag2));
        }

        @Test
        @DisplayName("Should remove tag")
        void testRemoveTag() {
            contact.getTags().add(tag1);
            contact.getTags().add(tag2);
            contact.getTags().remove(tag1);
            
            assertEquals(1, contact.getTags().size());
            assertFalse(contact.getTags().contains(tag1));
            assertTrue(contact.getTags().contains(tag2));
        }

        @Test
        @DisplayName("Should clear all tags")
        void testClearAllTags() {
            contact.getTags().add(tag1);
            contact.getTags().add(tag2);
            contact.getTags().clear();
            
            assertEquals(0, contact.getTags().size());
        }

        @Test
        @DisplayName("Should handle duplicate tags (Set behavior)")
        void testDuplicateTags() {
            contact.getTags().add(tag1);
            contact.getTags().add(tag1); // Trying to add same tag again
            
            assertEquals(1, contact.getTags().size()); // Set prevents duplicates
        }

        @Test
        @DisplayName("Should get tags as Set")
        void testGetTagsAsSet() {
            contact.getTags().add(tag1);
            contact.getTags().add(tag2);
            
            Set<ContactTag> tags = contact.getTags();
            assertTrue(tags instanceof Set);
            assertEquals(2, tags.size());
        }
    }

    @Nested
    @DisplayName("Social Links Management Tests")
    class SocialLinksManagementTests {
        
        @Test
        @DisplayName("Should have empty social links by default")
        void testDefaultEmptySocialLinks() {
            Contact newContact = new Contact();
            assertNotNull(newContact.getSocialLinks());
            assertEquals(0, newContact.getSocialLinks().size());
        }

        @Test
        @DisplayName("Should add single social link")
        void testAddSingleSocialLink() {
            contact.getSocialLinks().add(socialLink);
            assertEquals(1, contact.getSocialLinks().size());
            assertTrue(contact.getSocialLinks().contains(socialLink));
        }

        @Test
        @DisplayName("Should add multiple social links")
        void testAddMultipleSocialLinks() {
            SocialLink socialLink2 = SocialLink.builder()
                    .id(2L)
                    .title("LinkedIn")
                    .link("https://linkedin.com/in/example")
                    .build();
            
            contact.getSocialLinks().add(socialLink);
            contact.getSocialLinks().add(socialLink2);
            
            assertEquals(2, contact.getSocialLinks().size());
            assertTrue(contact.getSocialLinks().contains(socialLink));
            assertTrue(contact.getSocialLinks().contains(socialLink2));
        }

        @Test
        @DisplayName("Should remove social link")
        void testRemoveSocialLink() {
            contact.getSocialLinks().add(socialLink);
            contact.getSocialLinks().remove(socialLink);
            
            assertEquals(0, contact.getSocialLinks().size());
        }

        @Test
        @DisplayName("Should maintain order of social links (List behavior)")
        void testSocialLinksOrder() {
            SocialLink link1 = SocialLink.builder().id(1L).title("Twitter").build();
            SocialLink link2 = SocialLink.builder().id(2L).title("LinkedIn").build();
            SocialLink link3 = SocialLink.builder().id(3L).title("GitHub").build();
            
            contact.getSocialLinks().add(link1);
            contact.getSocialLinks().add(link2);
            contact.getSocialLinks().add(link3);
            
            List<SocialLink> links = contact.getSocialLinks();
            assertEquals("Twitter", links.get(0).getTitle());
            assertEquals("LinkedIn", links.get(1).getTitle());
            assertEquals("GitHub", links.get(2).getTitle());
        }
    }

    @Nested
    @DisplayName("Favorite Status Tests")
    class FavoriteStatusTests {
        
        @Test
        @DisplayName("Should have favorite as false by default")
        void testDefaultFavorite() {
            Contact newContact = new Contact();
            assertFalse(newContact.isFavorite());
        }

        @Test
        @DisplayName("Should set contact as favorite")
        void testSetFavorite() {
            contact.setFavorite(true);
            assertTrue(contact.isFavorite());
        }

        @Test
        @DisplayName("Should unset contact as favorite")
        void testUnsetFavorite() {
            contact.setFavorite(false);
            assertFalse(contact.isFavorite());
        }

        @Test
        @DisplayName("Should toggle favorite status")
        void testToggleFavorite() {
            assertFalse(contact.isFavorite());
            contact.setFavorite(true);
            assertTrue(contact.isFavorite());
            contact.setFavorite(false);
            assertFalse(contact.isFavorite());
        }
    }

    @Nested
    @DisplayName("Relationship Enum Tests")
    class RelationshipEnumTests {
        
        @Test
        @DisplayName("Should support FRIEND relationship")
        void testFriendRelationship() {
            contact.setRelationship(Relationship.FRIEND);
            assertEquals(Relationship.FRIEND, contact.getRelationship());
        }

        @Test
        @DisplayName("Should support FAMILY relationship")
        void testFamilyRelationship() {
            contact.setRelationship(Relationship.FAMILY);
            assertEquals(Relationship.FAMILY, contact.getRelationship());
        }

        @Test
        @DisplayName("Should support COLLEAGUE relationship")
        void testColleagueRelationship() {
            contact.setRelationship(Relationship.COLLEAGUE);
            assertEquals(Relationship.COLLEAGUE, contact.getRelationship());
        }

        @Test
        @DisplayName("Should support CLASSMATE relationship")
        void testClassmateRelationship() {
            contact.setRelationship(Relationship.CLASSMATE);
            assertEquals(Relationship.CLASSMATE, contact.getRelationship());
        }

        @Test
        @DisplayName("Should handle null relationship")
        void testNullRelationship() {
            contact.setRelationship(null);
            assertNull(contact.getRelationship());
        }
    }

    @Nested
    @DisplayName("Relationships with Other Entities Tests")
    class EntityRelationshipsTests {
        
        @Test
        @DisplayName("Should have reference to User")
        void testUserReference() {
            assertNotNull(contact.getUser());
            assertEquals("user123", contact.getUser().getId());
            assertEquals("John Doe", contact.getUser().getName());
        }

        @Test
        @DisplayName("Should have reference to Address")
        void testAddressReference() {
            assertNotNull(contact.getAddress());
            assertEquals("123 Main St", contact.getAddress().getStreet());
            assertEquals("Springfield", contact.getAddress().getCity());
        }

        @Test
        @DisplayName("Should handle null Address")
        void testNullAddress() {
            contact.setAddress(null);
            assertNull(contact.getAddress());
        }

        @Test
        @DisplayName("Should handle null User")
        void testNullUser() {
            contact.setUser(null);
            assertNull(contact.getUser());
        }

        @Test
        @DisplayName("Should update address connected to contact")
        void testUpdateAddress() {
            Address newAddress = Address.builder()
                    .id(2L)
                    .street("999 New St")
                    .city("New City")
                    .state("CA")
                    .country("USA")
                    .zipCode("90210")
                    .build();
            
            contact.setAddress(newAddress);
            
            assertEquals("999 New St", contact.getAddress().getStreet());
            assertEquals("New City", contact.getAddress().getCity());
        }
    }

    @Nested
    @DisplayName("Null & Empty Values Tests")
    class NullAndEmptyTests {
        
        @Test
        @DisplayName("Should handle null name")
        void testNullName() {
            contact.setName(null);
            assertNull(contact.getName());
        }

        @Test
        @DisplayName("Should handle empty email")
        void testEmptyEmail() {
            contact.setEmail("");
            assertEquals("", contact.getEmail());
        }

        @Test
        @DisplayName("Should handle null phoneNumber")
        void testNullPhone() {
            contact.setPhoneNumber(null);
            assertNull(contact.getPhoneNumber());
        }

        @Test
        @DisplayName("Should handle long content with 1000 char limit")
        void testLongAbout() {
            String longAbout = "a".repeat(1000);
            contact.setAbout(longAbout);
            assertEquals(1000, contact.getAbout().length());
        }

        @Test
        @DisplayName("Should handle null imageUrl")
        void testNullImageUrl() {
            contact.setImageUrl(null);
            assertNull(contact.getImageUrl());
        }
    }

    @Nested
    @DisplayName("Combined Functionality Tests")
    class CombinedFunctionalityTests {
        
        @Test
        @DisplayName("Should create full contact with all details")
        void testCreateFullContact() {
            // Already setup in the main contact
            assertNotNull(contact.getId());
            assertNotNull(contact.getName());
            assertNotNull(contact.getEmail());
            assertNotNull(contact.getPhoneNumber());
            assertNotNull(contact.getAddress());
            assertNotNull(contact.getUser());
            assertNotNull(contact.getRelationship());
        }

        @Test
        @DisplayName("Should update contact completely")
        void testUpdateContactCompletely() {
            contact.setName("New Name");
            contact.setEmail("new@example.com");
            contact.setPhoneNumber("0000000000");
            contact.setFavorite(true);
            contact.getTags().add(tag1);
            contact.getSocialLinks().add(socialLink);
            
            assertEquals("New Name", contact.getName());
            assertEquals("new@example.com", contact.getEmail());
            assertEquals("0000000000", contact.getPhoneNumber());
            assertTrue(contact.isFavorite());
            assertEquals(1, contact.getTags().size());
            assertEquals(1, contact.getSocialLinks().size());
        }

        @Test
        @DisplayName("Should maintain contact with tags and social links")
        void testContactWithTagsAndLinks() {
            contact.getTags().add(tag1);
            contact.getTags().add(tag2);
            contact.getSocialLinks().add(socialLink);
            
            assertEquals(2, contact.getTags().size());
            assertEquals(1, contact.getSocialLinks().size());
            assertTrue(contact.getTags().contains(tag1));
            assertTrue(contact.getTags().stream().anyMatch(t -> "Family".equals(t.getName())));
        }
    }
}
