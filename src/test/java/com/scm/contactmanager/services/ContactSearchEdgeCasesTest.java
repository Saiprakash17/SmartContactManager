package com.scm.contactmanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.scm.contactmanager.BaseIntegrationTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.ContactRepo;
import com.scm.contactmanager.config.CommonTestConfig;
import com.scm.contactmanager.config.TestSecurityConfig;

@SpringBootTest(classes = {CommonTestConfig.class})
@AutoConfigureTestDatabase
@Import({TestSecurityConfig.class})
class ContactSearchEdgeCasesTest extends BaseIntegrationTest {

    @Autowired
    private ContactService contactService;

    @MockBean
    private ContactRepo contactRepository;

    private User testUser;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");

        testContact = new Contact();
        testContact.setId(1L);
        testContact.setName("Test Contact");
        testContact.setEmail("contact@example.com");
        testContact.setUser(testUser);
    }

    @Test
    void shouldHandleSpecialCharactersInSearch() {
        Contact specialCharContact = new Contact();
        specialCharContact.setName("Test#$%^&*()_+ Contact");
        specialCharContact.setEmail("special@example.com");
        specialCharContact.setUser(testUser);

        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(specialCharContact));

        when(contactRepository.findByUserAndNameContaining(any(), any(), any()))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByName("#$%^&*()_+", 10, 0, "name", "asc", testUser);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldHandleVeryLongSearchString() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("Very Long Name ");
        }

        Contact longNameContact = new Contact();
        longNameContact.setName(longName.toString());
        longNameContact.setEmail("long@example.com");
        longNameContact.setUser(testUser);

        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(longNameContact));

        when(contactRepository.findByUserAndNameContaining(any(), any(), any()))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByName(longName.toString(), 10, 0, "name", "asc", testUser);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldHandleEmptySearchString() {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));

        when(contactRepository.findByUser(any(), any()))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByName("", 10, 0, "name", "asc", testUser);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldHandleUnicodeCharacters() {
        Contact unicodeContact = new Contact();
        unicodeContact.setName("测试联系人");
        unicodeContact.setEmail("unicode@example.com");
        unicodeContact.setUser(testUser);

        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(unicodeContact));

        when(contactRepository.findByUserAndNameContaining(any(), any(), any()))
            .thenReturn(contactPage);

        Page<Contact> result = contactService.searchByName("测试", 10, 0, "name", "asc", testUser);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
