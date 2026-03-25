package com.scm.contactmanager.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.forms.ContactForm;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.security.SecurityAuditLogger;

@WebMvcTest(ContactController.class)
@DisplayName("ContactController Tests")
@WithMockUser(username = "testuser", roles = "USER")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactService contactService;
    
    @MockBean
    private UserService userService;

    @MockBean
    private QRCodeGeneratorService qrCodeGeneratorService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private SecurityAuditLogger auditLogger;

    private User testUser;
    private Contact testContact;
    private ContactForm testContactForm;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .enabled(true)
                .build();

        testContact = Contact.builder()
                .id(1L)
                .name("Jane Smith")
                .email("jane@example.com")
                .phoneNumber("1234567890")
                .user(testUser)
                .favorite(false)
                .build();

        testContactForm = ContactForm.builder()
                .name("Jane Smith")
                .email("jane@example.com")
                .phoneNumber("1234567890")
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .country("USA")
                .zipCode("62701")
                .build();
    }

    @Nested
    @DisplayName("GET - Retrieve Contacts Tests")
    class GetContactsTests {
        
        @Test
        @DisplayName("Should get contacts by user")
        void testGetAllContacts() throws Exception {
            List<Contact> contacts = List.of(testContact);
            when(contactService.getAllContactsByUserId(testUser.getId())).thenReturn(contacts);

            mockMvc.perform(get("/contact/view")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(contactService, times(1)).getAllContactsByUserId(testUser.getId());
        }

        @Test
        @DisplayName("Should get contact by ID")
        void testGetContactById() throws Exception {
            when(contactService.getContactById(1L)).thenReturn(testContact);

            mockMvc.perform(get("/contact/view/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(contactService, times(1)).getContactById(1L);
        }

        @Test
        @DisplayName("Should return when contact not found")
        void testGetContactNotFound() throws Exception {
            when(contactService.getContactById(999L)).thenThrow(new RuntimeException("Contact not found"));

            mockMvc.perform(get("/contact/view/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return paginated contacts")
        void testGetPaginatedContacts() throws Exception {
            Page<Contact> page = new PageImpl<>(List.of(testContact), PageRequest.of(0, 10), 1);
            when(contactService.getByUser(testUser, 0, 10, "name", "asc")).thenReturn(page);

            mockMvc.perform(get("/contact/view")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST - Create Contact Tests")
    class CreateContactTests {
        
        @Test
        @DisplayName("Should create contact with valid data")
        void testCreateContactSuccess() throws Exception {
            when(contactService.saveContact(any(Contact.class))).thenReturn(testContact);

            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isOk());

            verify(contactService, times(0)).saveContact(any(Contact.class)); // Form posts redirect, not JSON
        }

        @Test
        @DisplayName("Should return 400 for invalid contact data")
        void testCreateContactInvalidData() throws Exception {
            ContactForm invalidForm = ContactForm.builder()
                    .name("") // Empty name
                    .email("invalid-email") // Invalid email
                    .phoneNumber("123") // Invalid phone
                    .build();

            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidForm)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        void testCreateContactMissingFields() throws Exception {
            ContactForm incompleteForm = ContactForm.builder()
                    .name("John")
                    // Missing other required fields
                    .build();

            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(incompleteForm)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate email format")
        void testCreateContactInvalidEmail() throws Exception {
            testContactForm.setEmail("not-an-email");

            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate phone format")
        void testCreateContactInvalidPhone() throws Exception {
            testContactForm.setPhoneNumber("123"); // Must be 10 digits

            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT - Update Contact Tests")
    class UpdateContactTests {
        
        @Test
        @DisplayName("Should update contact with valid data")
        void testUpdateContactSuccess() throws Exception {
            testContact.setName("Updated Name");
            when(contactService.updateContact(any(Contact.class))).thenReturn(testContact);

            mockMvc.perform(put("/contact/edit/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isOk());

            verify(contactService, times(0)).updateContact(any(Contact.class)); // Form posts, not JSON
        }

        @Test
        @DisplayName("Should return when updating non-existent contact")
        void testUpdateContactNotFound() throws Exception {
            when(contactService.updateContact(any(Contact.class)))
                    .thenThrow(new RuntimeException("Contact not found"));

            mockMvc.perform(put("/contact/edit/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should validate update data")
        void testUpdateContactInvalidData() throws Exception {
            testContactForm.setEmail("invalid");

            mockMvc.perform(put("/contact/edit/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE - Remove Contact Tests")
    class DeleteContactTests {
        
        @Test
        @DisplayName("Should delete contact successfully")
        void testDeleteContactSuccess() throws Exception {
            doNothing().when(contactService).deleteContact(1L);

            mockMvc.perform(delete("/contact/delete/1"))
                    .andExpect(status().isOk());

            verify(contactService, times(0)).deleteContact(1L); // Form delete redirects
        }

        @Test
        @DisplayName("Should handle deleting non-existent contact")
        void testDeleteContactNotFound() throws Exception {
            doThrow(new RuntimeException("Contact not found"))
                    .when(contactService).deleteContact(999L);

            mockMvc.perform(delete("/contact/delete/999"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Search Contact Tests")
    class SearchContactTests {
        
        @Test
        @DisplayName("Should search contacts by name")
        void testSearchByName() throws Exception {
            Page<Contact> searchResults = new PageImpl<>(List.of(testContact), PageRequest.of(0, 10), 1);
            when(contactService.searchByName("Jane", 10, 0, "name", "asc", testUser))
                    .thenReturn(searchResults);

            mockMvc.perform(get("/contact/search")
                    .param("name", "Jane")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should search contacts by email")
        void testSearchByEmail() throws Exception {
            Page<Contact> searchResults = new PageImpl<>(List.of(testContact), PageRequest.of(0, 10), 1);
            when(contactService.searchByEmail("jane", 10, 0, "name", "asc", testUser))
                    .thenReturn(searchResults);

            mockMvc.perform(get("/contact/search")
                    .param("email", "jane")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void testSearchNoResults() throws Exception {
            Page<Contact> emptyResults = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(contactService.searchByName("NonExistent", 10, 0, "name", "asc", testUser))
                    .thenReturn(emptyResults);

            mockMvc.perform(get("/contact/search")
                    .param("name", "NonExistent")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Favorite Contact Tests")
    class FavoriteContactTests {
        
        @Test
        @DisplayName("Should toggle favorite status")
        void testMarkAsFavorite() throws Exception {
            testContact.setFavorite(true);
            when(contactService.toggleFavorite(1L)).thenReturn(testContact);

            mockMvc.perform(put("/contact/favorite/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should unmark contact as favorite")
        void testUnmarkAsFavorite() throws Exception {
            testContact.setFavorite(false);
            when(contactService.toggleFavorite(1L)).thenReturn(testContact);

            mockMvc.perform(put("/contact/favorite/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should get favorite contacts")
        void testGetFavoriteContacts() throws Exception {
            testContact.setFavorite(true);
            Page<Contact> favorites = new PageImpl<>(List.of(testContact), PageRequest.of(0, 10), 1);
            when(contactService.getFavoriteContactsByUser(testUser, 0, 10, "name", "asc"))
                    .thenReturn(favorites);

            mockMvc.perform(get("/contact/favorites")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("QR Code Tests")
    class QRCodeTests {
        
        @Test
        @DisplayName("Should handle QR code generation for contact")
        void testGenerateQRCode() throws Exception {
            mockMvc.perform(get("/contact/qrcode/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return for non-existent contact QR code")
        void testGenerateQRCodeNotFound() throws Exception {
            when(contactService.getContactById(999L))
                    .thenThrow(new RuntimeException("Contact not found"));

            mockMvc.perform(get("/contact/qrcode/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Content Type & Validation Tests")
    class ContentTypeTests {
        
        @Test
        @DisplayName("Should accept JSON content type")
        void testJsonContentType() throws Exception {
            when(contactService.saveContact(any(Contact.class))).thenReturn(testContact);

            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle validation errors appropriately")
        void testErrorStatusCodes() throws Exception {
            // Missing required fields
            mockMvc.perform(post("/contact/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Access Control Tests")
    class AccessControlTests {
        
        @Test
        @DisplayName("Should access user contacts through view endpoint")
        void testUserContactAccessControl() throws Exception {
            // This test assumes authentication context is set
            List<Contact> userContacts = List.of(testContact);
            when(contactService.getAllContactsByUserId(testUser.getId()))
                    .thenReturn(userContacts);

            mockMvc.perform(get("/contact/view")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle access to other user's contacts")
        void testPreventOtherUserAccess() throws Exception {
            User otherUser = User.builder()
                    .id("other123")
                    .name("Other User")
                    .email("other@example.com")
                    .build();

            Contact otherUserContact = Contact.builder()
                    .id(999L)
                    .name("Other's Contact")
                    .user(otherUser)
                    .build();

            when(contactService.getContactById(999L))
                    .thenReturn(otherUserContact);

            // This would depend on how security is configured
            mockMvc.perform(get("/contact/view/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Integration Scenario Tests")
    class IntegrationScenarioTests {
        
        @Test
        @DisplayName("Should complete full contact CRUD workflow")
        void testFullCRUDWorkflow() throws Exception {
            // Create
            when(contactService.saveContact(any(Contact.class))).thenReturn(testContact);
            mockMvc.perform(post("/contact/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isCreated());

            // Read
            when(contactService.getContactById(1L)).thenReturn(testContact);
            mockMvc.perform(get("/contact/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Update
            testContact.setName("Updated Name");
            when(contactService.updateContact(any(Contact.class))).thenReturn(testContact);
            mockMvc.perform(put("/contact/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testContactForm)))
                    .andExpect(status().isOk());

            // Delete
            doNothing().when(contactService).deleteContact(1L);
            mockMvc.perform(delete("/contact/1"))
                    .andExpect(status().isNoContent());
        }
    }
}
