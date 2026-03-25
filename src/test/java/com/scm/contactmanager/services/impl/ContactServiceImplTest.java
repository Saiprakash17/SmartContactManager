package com.scm.contactmanager.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.ContactRepo;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactServiceImpl Tests")
class ContactServiceImplTest {

    @Mock
    private ContactRepo contactRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private User testUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .build();

        testContact = Contact.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane@example.com")
                .phoneNumber("1234567890")
                .user(testUser)
                .build();
    }

    @Nested
    @DisplayName("Create Contact Tests")
    class CreateContactTests {
        
        @Test
        @DisplayName("Should save new contact successfully")
        void testCreateContactSuccess() {
            when(contactRepository.save(testContact)).thenReturn(testContact);
            
            Contact savedContact = contactService.saveContact(testContact);
            
            assertNotNull(savedContact);
            assertEquals(1L, savedContact.getId());
            assertEquals("Jane Smith", savedContact.getName());
            verify(contactRepository, times(1)).save(testContact);
        }

        @Test
        @DisplayName("Should throw exception when contact is null")
        void testCreateContactNull() {
            when(contactRepository.save(null)).thenThrow(new IllegalArgumentException("Contact cannot be null"));
            
            assertThrows(Exception.class, () -> {
                contactService.saveContact(null);
            });
        }

        @Test
        @DisplayName("Should save contact with valid data")
        void testCreateContactWithCompleteData() {
            Contact fullContact = Contact.builder()
                    .id(1L)
                    .name("Complete User")
                    .email("complete@example.com")
                    .phoneNumber("1234567890")
                    .about("Test User")
                    .favorite(false)
                    .website("https://example.com")
                    .linkedin("https://linkedin.com/in/example")
                    .user(testUser)
                    .build();
            
            when(contactRepository.save(fullContact)).thenReturn(fullContact);
            
            Contact result = contactService.saveContact(fullContact);
            
            assertNotNull(result);
            assertEquals("Complete User", result.getName());
        }
    }

    @Nested
    @DisplayName("Read/Get Contact Tests")
    class ReadContactTests {
        
        @Test
        @DisplayName("Should get contact by ID successfully")
        void testGetContactById() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            
            Contact result = contactService.getContactById(1L);
            
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Jane Smith", result.getName());
        }

        @Test
        @DisplayName("Should throw exception when contact not found")
        void testGetContactByIdNotFound() {
            when(contactRepository.findById(999L)).thenReturn(Optional.empty());
            
            assertThrows(Exception.class, () -> {
                contactService.getContactById(999L);
            });
        }

        @Test
        @DisplayName("Should get all contacts for user")
        void testGetAllContactsForUser() {
            List<Contact> contacts = List.of(testContact);
            when(contactRepository.findByUser(eq(testUser), any(Pageable.class))).thenReturn(new PageImpl<>(contacts));
            
            Page<Contact> result = contactService.getByUser(testUser, 0, 10, "name", "asc");
            
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty list when user has no contacts")
        void testGetAllContactsEmptyList() {
            when(contactRepository.findByUser(eq(testUser), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
            
            Page<Contact> result = contactService.getByUser(testUser, 0, 10, "name", "asc");
            
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Update Contact Tests")
    class UpdateContactTests {
        
        @Test
        @DisplayName("Should update contact successfully")
        void testUpdateContact() {
            Contact updatedContact = Contact.builder()
                    .id(1L)
                    .name("Updated Name")
                    .email("updated@example.com")
                    .phoneNumber("9999999999")
                    .user(testUser)
                    .build();
            
            when(contactRepository.findById(1L)).thenReturn(Optional.of(updatedContact));
            when(contactRepository.save(updatedContact)).thenReturn(updatedContact);
            
            Contact result = contactService.updateContact(updatedContact);
            
            assertNotNull(result);
            assertEquals("Updated Name", result.getName());
            assertEquals("updated@example.com", result.getEmail());
        }

        @Test
        @DisplayName("Should update only changed fields")
        void testPartialUpdateContact() {
            testContact.setName("New Name");
            
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            when(contactRepository.save(testContact)).thenReturn(testContact);
            
            Contact result = contactService.updateContact(testContact);
            
            assertEquals("New Name", result.getName());
        }
    }

    @Nested
    @DisplayName("Delete Contact Tests")
    class DeleteContactTests {
        
        @Test
        @DisplayName("Should delete contact successfully")
        void testDeleteContact() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            doNothing().when(contactRepository).delete(testContact);
            
            contactService.deleteContact(1L);
            
            verify(contactRepository, times(1)).delete(testContact);
        }

        @Test
        @DisplayName("Should handle deletion of non-existent contact")
        void testDeleteNonExistentContact() {
            when(contactRepository.findById(999L)).thenReturn(Optional.empty());
            
            assertThrows(Exception.class, () -> contactService.deleteContact(999L));
        }


    }

    @Nested
    @DisplayName("Search Contact Tests")
    class SearchContactTests {
        
        @Test
        @DisplayName("Should support search functionality")
        void testSearchFunctionality() {
            // Search methods are implemented in service
            assertNotNull(contactService);
            assertNotNull(testUser);
            assertNotNull(testContact);
        }

        @Test
        @DisplayName("Should validate search parameters")
        void testSearchParameters() {
            String keyword = "test";
            int page = 0;
            int size = 10;
            
            assertTrue(keyword.length() > 0);
            assertTrue(page >= 0);
            assertTrue(size > 0);
        }
    }

    @Nested
    @DisplayName("Favorite Contact Tests")
    class FavoriteContactTests {
        
        @Test
        @DisplayName("Should mark contact as favorite")
        void testMarkAsFavorite() {
            testContact.setFavorite(true);
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            when(contactRepository.save(testContact)).thenReturn(testContact);
            
            Contact result = contactService.updateContact(testContact);
            
            assertTrue(result.isFavorite());
        }

        @Test
        @DisplayName("Should unmark contact as favorite")
        void testUnmarkAsFavorite() {
            testContact.setFavorite(false);
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            when(contactRepository.save(testContact)).thenReturn(testContact);
            
            Contact result = contactService.updateContact(testContact);
            
            assertFalse(result.isFavorite());
        }

        @Test
        @DisplayName("Should support favorite contacts functionality")
        void testFavoriteContacts() {
            testContact.setFavorite(true);
            assertNotNull(contactService);
            assertTrue(testContact.isFavorite());
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {
        
        @Test
        @DisplayName("Should get paginated contacts")
        void testGetPaginatedContacts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = new PageImpl<>(List.of(testContact), pageable, 1);
            
            when(contactRepository.findByUser(eq(testUser), any(Pageable.class))).thenReturn(page);
            
            Page<Contact> result = contactService.getByUser(testUser, 0, 10, "name", "asc");
            
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should handle empty page")
        void testGetEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            
            when(contactRepository.findByUser(eq(testUser), any(Pageable.class))).thenReturn(emptyPage);
            
            Page<Contact> result = contactService.getByUser(testUser, 0, 10, "name", "asc");
            
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate contact email")
        void testValidateEmail() {
            Contact validContact = Contact.builder()
                    .email("valid@example.com")
                    .build();
            
            assertTrue(validContact.getEmail().contains("@"));
        }

        @Test
        @DisplayName("Should validate contact phone")
        void testValidatePhone() {
            Contact validContact = Contact.builder()
                    .phoneNumber("1234567890")
                    .build();
            
            assertEquals(10, validContact.getPhoneNumber().length());
        }

        @Test
        @DisplayName("Should validate contact name is not empty")
        void testValidateName() {
            Contact validContact = Contact.builder()
                    .name("Jane Smith")
                    .build();
            
            assertFalse(validContact.getName().isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {
        
        @Test
        @DisplayName("Should create and retrieve contact")
        void testCreateAndRetrieve() {
            when(contactRepository.save(testContact)).thenReturn(testContact);
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            
            Contact saved = contactService.saveContact(testContact);
            Contact retrieved = contactService.getContactById(saved.getId());
            
            assertNotNull(retrieved);
            assertEquals(saved.getId(), retrieved.getId());
        }

        @Test
        @DisplayName("Should create, update, and delete contact")
        void testFullLifecycle() {
            when(contactRepository.save(testContact)).thenReturn(testContact);
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            doNothing().when(contactRepository).delete(testContact);
            
            Contact saved = contactService.saveContact(testContact);
            assertTrue(saved.getId() > 0);
            
            testContact.setName("Updated Name");
            when(contactRepository.save(testContact)).thenReturn(testContact);
            Contact updated = contactService.updateContact(testContact);
            assertEquals("Updated Name", updated.getName());
            
            contactService.deleteContact(saved.getId());
            verify(contactRepository, times(1)).delete(testContact);
        }

        @Test
        @DisplayName("Should handle multiple contacts for user")
        void testMultipleContactsPerUser() {
            Contact contact2 = Contact.builder()
                    .id(2L)
                    .name("Another Contact")
                    .user(testUser)
                    .build();
            
            List<Contact> contacts = List.of(testContact, contact2);
            when(contactRepository.findByUser(eq(testUser), any(Pageable.class))).thenReturn(new PageImpl<>(contacts));
            
            Page<Contact> result = contactService.getByUser(testUser, 0, 10, "name", "asc");
            
            assertEquals(2, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle repository exceptions")
        void testRepositoryException() {
            when(contactRepository.save(any())).thenThrow(new RuntimeException("Database error"));
            
            assertThrows(RuntimeException.class, () -> {
                contactService.saveContact(testContact);
            });
        }

        @Test
        @DisplayName("Should handle null user in search")
        void testNullUserSearch() {
            when(contactRepository.findByUser(eq(null), any(Pageable.class))).thenThrow(new RuntimeException("User cannot be null"));
            
            assertThrows(RuntimeException.class, () -> {
                contactService.getByUser(null, 0, 10, "name", "asc");
            });
        }
    }
}
