package com.scm.contactmanager.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ContactTag Entity Tests")
class ContactTagTest {

    private ContactTag tag;

    @BeforeEach
    void setUp() {
        tag = ContactTag.builder()
                .id(1L)
                .name("Family")
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create tag with NoArgsConstructor")
        void testNoArgsConstructor() {
            ContactTag newTag = new ContactTag();
            assertNotNull(newTag);
        }

        @Test
        @DisplayName("Should create tag with Builder")
        void testBuilder() {
            assertNotNull(tag);
            assertEquals(1L, tag.getId());
            assertEquals("Family", tag.getName());
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            tag.setId(999L);
            assertEquals(999L, tag.getId());
        }

        @Test
        @DisplayName("Should get and set name")
        void testNameGetterSetter() {
            tag.setName("Work");
            assertEquals("Work", tag.getName());
        }
    }

    @Nested
    @DisplayName("Common Tags Tests")
    class CommonTagsTests {
        
        @Test
        @DisplayName("Should support Family tag")
        void testFamilyTag() {
            assertEquals("Family", tag.getName());
        }

        @Test
        @DisplayName("Should support Work tag")
        void testWorkTag() {
            tag.setName("Work");
            assertEquals("Work", tag.getName());
        }

        @Test
        @DisplayName("Should support Friends tag")
        void testFriendsTag() {
            tag.setName("Friends");
            assertEquals("Friends", tag.getName());
        }

        @Test
        @DisplayName("Should support custom tag names")
        void testCustomTagName() {
            tag.setName("Close Friends");
            assertEquals("Close Friends", tag.getName());
        }
    }

    @Nested
    @DisplayName("Null & Empty Tests")
    class NullAndEmptyTests {
        
        @Test
        @DisplayName("Should handle null name")
        void testNullName() {
            tag.setName(null);
            assertNull(tag.getName());
        }

        @Test
        @DisplayName("Should handle empty name")
        void testEmptyName() {
            tag.setName("");
            assertEquals("", tag.getName());
        }
    }
}

@DisplayName("SocialLink Entity Tests")
class SocialLinkTest {

    private SocialLink socialLink;

    @BeforeEach
    void setUp() {
        socialLink = SocialLink.builder()
                .id(1L)
                .title("Twitter")
                .link("https://twitter.com/example")
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create social link with Builder")
        void testBuilder() {
            assertNotNull(socialLink);
            assertEquals(1L, socialLink.getId());
            assertEquals("Twitter", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should create social link with NoArgsConstructor")
        void testNoArgsConstructor() {
            SocialLink newLink = new SocialLink();
            assertNotNull(newLink);
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            socialLink.setId(999L);
            assertEquals(999L, socialLink.getId());
        }

        @Test
        @DisplayName("Should get and settitle")
        void testitleGetterSetter() {
            socialLink.setTitle("LinkedIn");
            assertEquals("LinkedIn", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should get and set link")
        void testLinkGetterSetter() {
            socialLink.setLink("https://linkedin.com/in/example");
            assertEquals("https://linkedin.com/in/example", socialLink.getLink());
        }
    }

    @Nested
    @DisplayName("Socialtitle Tests")
    class SociatitleTests {
        
        @Test
        @DisplayName("Should support Twitter")
        void testTwitter() {
            socialLink.setTitle("Twitter");
            assertEquals("Twitter", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should support LinkedIn")
        void testLinkedIn() {
            socialLink.setTitle("LinkedIn");
            assertEquals("LinkedIn", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should support GitHub")
        void testGitHub() {
            socialLink.setTitle("GitHub");
            assertEquals("GitHub", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should support Instagram")
        void testInstagram() {
            socialLink.setTitle("Instagram");
            assertEquals("Instagram", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should support Facebook")
        void testFacebook() {
            socialLink.setTitle("Facebook");
            assertEquals("Facebook", socialLink.getTitle());
        }

        @Test
        @DisplayName("Should support customtitle")
        void testCustotitle() {
            socialLink.setTitle("TikTok");
            assertEquals("TikTok", socialLink.getTitle());
        }
    }

    @Nested
    @DisplayName("Social Link URL Tests")
    class SocialLinkURLTests {
        
        @Test
        @DisplayName("Should accept valid HTTPS URL")
        void testHTTPSUrl() {
            assertTrue(socialLink.getLink().startsWith("https://"));
        }

        @Test
        @DisplayName("Should accept HTTP URL")
        void testHTTPUrl() {
            socialLink.setLink("http://twitter.com/example");
            assertTrue(socialLink.getLink().startsWith("http://"));
        }

        @Test
        @DisplayName("Should handle URL with username")
        void testURLWithUsername() {
            socialLink.setLink("https://twitter.com/johndoe");
            assertTrue(socialLink.getLink().contains("johndoe"));
        }
    }
}

@DisplayName("ImportantDate Entity Tests")
class ImportantDateTest {

    private ImportantDate importantDate;

    @BeforeEach
    void setUp() {
        importantDate = ImportantDate.builder()
                .id(1L)
                .name("Birthday")
                .date(LocalDate.of(1990, 5, 15))
                .build();
    }

    @Nested
    @DisplayName("Constructor & Builder Tests")
    class ConstructorAndBuilderTests {
        
        @Test
        @DisplayName("Should create important date with Builder")
        void testBuilder() {
            assertNotNull(importantDate);
            assertEquals(1L, importantDate.getId());
            assertEquals("Birthday", importantDate.getName());
        }

        @Test
        @DisplayName("Should create important date with NoArgsConstructor")
        void testNoArgsConstructor() {
            ImportantDate newDate = new ImportantDate();
            assertNotNull(newDate);
        }
    }

    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set ID")
        void testIdGetterSetter() {
            importantDate.setId(999L);
            assertEquals(999L, importantDate.getId());
        }

        @Test
        @DisplayName("Should get and set title")
        void testTitleGetterSetter() {
            importantDate.setName("Anniversary");
            assertEquals("Anniversary", importantDate.getName());
        }

        @Test
        @DisplayName("Should get and set date")
        void testDateGetterSetter() {
            LocalDate expectedDate = LocalDate.of(2020, 12, 25);
            importantDate.setDate(expectedDate);
            assertEquals(expectedDate, importantDate.getDate());
        }
    }

    @Nested
    @DisplayName("Important Date Types Tests")
    class ImportantDateTypesTests {
        
        @Test
        @DisplayName("Should support Birthday")
        void testBirthday() {
            importantDate.setName("Birthday");
            assertEquals("Birthday", importantDate.getName());
        }

        @Test
        @DisplayName("Should support Anniversary")
        void testAnniversary() {
            importantDate.setName("Anniversary");
            assertEquals("Anniversary", importantDate.getName());
        }

        @Test
        @DisplayName("Should support custom important date")
        void testCustomDate() {
            importantDate.setName("Graduation");
            assertEquals("Graduation", importantDate.getName());
        }
    }

    @Nested
    @DisplayName("Date Format Tests")
    class DateFormatTests {
        
        @Test
        @DisplayName("Should accept date in YYYY-MM-DD format")
        void testYYYYMMDDFormat() {
            assertEquals(LocalDate.of(1990, 5, 15), importantDate.getDate());
        }

        @Test
        @DisplayName("Should handle different dates")
        void testDifferentDates() {
            importantDate.setDate(LocalDate.of(2023, 1, 1));
            assertEquals(LocalDate.of(2023, 1, 1), importantDate.getDate());
        }
    }
}
