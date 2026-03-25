package com.scm.contactmanager.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Relationship Enum Tests")
class RelationshipEnumTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {
        
        @Test
        @DisplayName("Should have FRIEND enum value")
        void testFriendValue() {
            assertNotNull(Relationship.FRIEND);
            assertEquals("FRIEND", Relationship.FRIEND.name());
        }

        @Test
        @DisplayName("Should have FAMILY enum value")
        void testFamilyValue() {
            assertNotNull(Relationship.FAMILY);
            assertEquals("FAMILY", Relationship.FAMILY.name());
        }

        @Test
        @DisplayName("Should have COLLEAGUE enum value")
        void testColleagueValue() {
            assertNotNull(Relationship.COLLEAGUE);
            assertEquals("COLLEAGUE", Relationship.COLLEAGUE.name());
        }

        @Test
        @DisplayName("Should have OTHER enum value")
        void testOtherValue() {
            assertNotNull(Relationship.OTHER);
            assertEquals("OTHER", Relationship.OTHER.name());
        }
    }

    @Nested
    @DisplayName("Enum Comparison Tests")
    class EnumComparisonTests {
        
        @Test
        @DisplayName("Should compare relationships correctly")
        void testRelationshipComparison() {
            Relationship rel1 = Relationship.FRIEND;
            Relationship rel2 = Relationship.FRIEND;
            
            assertEquals(rel1, rel2);
        }

        @Test
        @DisplayName("Should distinguish different relationships")
        void testDistinctRelationships() {
            Relationship rel1 = Relationship.FRIEND;
            Relationship rel2 = Relationship.COLLEAGUE;
            
            assertNotEquals(rel1, rel2);
        }
    }

    @Nested
    @DisplayName("Enum Iteration Tests")
    class EnumIterationTests {
        
        @Test
        @DisplayName("Should have all enum values")
        void testAllEnumValues() {
            Relationship[] values = Relationship.values();
            assertTrue(values.length >= 4);
        }

        @Test
        @DisplayName("Should find FRIEND from string")
        void testValueOf() {
            Relationship rel = Relationship.valueOf("FRIEND");
            assertEquals(Relationship.FRIEND, rel);
        }
    }
}

@DisplayName("Providers Enum Tests")
class ProvidersEnumTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {
        
        @Test
        @DisplayName("Should have SELF enum value")
        void testSelfValue() {
            assertNotNull(Providers.SELF);
            assertEquals("SELF", Providers.SELF.name());
        }

        @Test
        @DisplayName("Should have GOOGLE enum value")
        void testGoogleValue() {
            assertNotNull(Providers.GOOGLE);
            assertEquals("GOOGLE", Providers.GOOGLE.name());
        }

        @Test
        @DisplayName("Should have FACEBOOK enum value")
        void testFacebookValue() {
            assertNotNull(Providers.FACEBOOK);
            assertEquals("FACEBOOK", Providers.FACEBOOK.name());
        }

        @Test
        @DisplayName("Should have GITHUB enum value")
        void testGithubValue() {
            assertNotNull(Providers.GITHUB);
            assertEquals("GITHUB", Providers.GITHUB.name());
        }
    }

    @Nested
    @DisplayName("Enum Comparison Tests")
    class EnumComparisonTests {
        
        @Test
        @DisplayName("Should compare providers correctly")
        void testProviderComparison() {
            Providers prov1 = Providers.GOOGLE;
            Providers prov2 = Providers.GOOGLE;
            
            assertEquals(prov1, prov2);
        }

        @Test
        @DisplayName("Should distinguish different providers")
        void testDistinctProviders() {
            Providers prov1 = Providers.GOOGLE;
            Providers prov2 = Providers.FACEBOOK;
            
            assertNotEquals(prov1, prov2);
        }
    }

    @Nested
    @DisplayName("Enum Iteration Tests")
    class EnumIterationTests {
        
        @Test
        @DisplayName("Should have all enum values")
        void testAllEnumValues() {
            Providers[] values = Providers.values();
            assertTrue(values.length >= 4);
        }

        @Test
        @DisplayName("Should find SELF as default")
        void testSelfAsDefault() {
            Providers defaultProvider = Providers.SELF;
            assertNotNull(defaultProvider);
        }

        @Test
        @DisplayName("Should find GOOGLE from string")
        void testValueOf() {
            Providers prov = Providers.valueOf("GOOGLE");
            assertEquals(Providers.GOOGLE, prov);
        }
    }

    @Nested
    @DisplayName("OAuth Provider Tests")
    class OAuthProviderTests {
        
        @Test
        @DisplayName("Should support OAuth providers (GOOGLE, FACEBOOK, GITHUB)")
        void testOAuthProviders() {
            assertNotNull(Providers.GOOGLE);
            assertNotNull(Providers.FACEBOOK);
            assertNotNull(Providers.GITHUB);
        }

        @Test
        @DisplayName("Should distinguish SELF from OAuth providers")
        void testSelfVsOAuth() {
            assertNotEquals(Providers.SELF, Providers.GOOGLE);
            assertNotEquals(Providers.SELF, Providers.FACEBOOK);
            assertNotEquals(Providers.SELF, Providers.GITHUB);
        }
    }
}
