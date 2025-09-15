package com.scm.contactmanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.services.impl.ContactServiceImpl;
import com.scm.contactmanager.helper.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    private ContactRepo contactRepo;

    @InjectMocks
    private ContactServiceImpl contactService;

    private Contact testContact;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");

        testContact = new Contact();
        testContact.setId(1L);
        testContact.setName("Test Contact");
        testContact.setEmail("contact@example.com");
        testContact.setPhoneNumber("1234567890");
        testContact.setUser(testUser);
    }

    @Test
    void shouldSaveContact() {
        when(contactRepo.save(any(Contact.class))).thenReturn(testContact);

        Contact savedContact = contactService.saveContact(testContact);

        assertNotNull(savedContact);
        assertEquals(testContact.getName(), savedContact.getName());
        assertEquals(testContact.getEmail(), savedContact.getEmail());
        verify(contactRepo).save(any(Contact.class));
    }

    @Test
    void shouldGetContactById() {
        when(contactRepo.findById(testContact.getId())).thenReturn(Optional.of(testContact));

        Contact foundContact = contactService.getContactById(testContact.getId());

        assertNotNull(foundContact);
        assertEquals(testContact.getId(), foundContact.getId());
        verify(contactRepo).findById(testContact.getId());
    }

    @Test
    void shouldThrowExceptionWhenContactNotFound() {
        when(contactRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            contactService.getContactById(999L);
        });
    }

    @Test
    void shouldGetContactsByUser() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUser(eq(testUser), any(Pageable.class))).thenReturn(contactPage);

        Page<Contact> result = contactService.getByUser(testUser, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testContact.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldSearchContactsByName() {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUserAndNameContaining(eq(testUser), anyString(), any(Pageable.class)))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByName("Test", 10, 0, "name", "asc", testUser);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testContact.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldSearchContactsByEmail() {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUserAndEmailContaining(eq(testUser), anyString(), any(Pageable.class)))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByEmail("test@", 10, 0, "name", "asc", testUser);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testContact.getEmail(), result.getContent().get(0).getEmail());
    }

    @Test
    void shouldSearchContactsByPhoneNumber() {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUserAndPhoneNumberContaining(eq(testUser), anyString(), any(Pageable.class)))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByPhoneNumber("1234", 10, 0, "name", "asc", testUser);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testContact.getPhoneNumber(), result.getContent().get(0).getPhoneNumber());
    }

    @Test
    void shouldSearchFavoriteContactsByName() {
        testContact.setFavorite(true);
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUserAndFavoriteTrueAndNameContaining(eq(testUser), anyString(), any(Pageable.class)))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchFavoriteByName("Test", 10, 0, "name", "asc", testUser);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testContact.getName(), result.getContent().get(0).getName());
        assertTrue(result.getContent().get(0).isFavorite());
    }

    @Test
    void shouldGetAllFavoriteContacts() {
        testContact.setFavorite(true);
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUserAndFavoriteTrue(eq(testUser), any(Pageable.class)))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.getFavoriteContactsByUser(testUser, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isFavorite());
    }

    @Test
    void shouldGetFavoriteContacts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        testContact.setFavorite(true);
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        
        when(contactRepo.findByUserAndFavoriteTrue(eq(testUser), any(Pageable.class)))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.getFavoriteContactsByUser(testUser, 0, 10, "name", "asc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().get(0).isFavorite());
    }
}
