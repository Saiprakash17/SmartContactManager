package com.scm.contactmanager.controllers;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Arrays;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.helper.SessionHelper;
import com.scm.contactmanager.repositories.UserRepo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import com.scm.contactmanager.config.TestSecurityConfig;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.scm.contactmanager.services.UserService;

@WebMvcTest(ContactController.class)
@Import(TestSecurityConfig.class)
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private UserService userService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private QRCodeGeneratorService qrCodeGeneratorService;

    @MockBean(name = "sessionHelper")
    private SessionHelper sessionHelper;
    
    @MockBean
    private UserRepo userRepo;

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

        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDisplayContactsList() throws Exception {
        testContact.setAddress(null);
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getByUser(any(), any(Integer.class), any(Integer.class), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/view"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_contacts"))
            .andExpect(model().attributeExists("contactsPage"))
            .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDisplayAddContactForm() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);

        mockMvc.perform(get("/user/contacts/add"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/add_contact"))
            .andExpect(model().attributeExists("contactForm"))
            .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDisplayFavoriteContacts() throws Exception {
        testContact.setAddress(null);
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getFavoriteContactsByUser(any(), any(Integer.class), any(Integer.class), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/favorites"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_favorite_contacts"))
            .andExpect(model().attributeExists("contactsPage"))
            .andExpect(model().attributeExists("loggedInUser"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldAddNewContact() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.saveContact(any(Contact.class))).thenReturn(testContact);

        mockMvc.perform(post("/user/contacts/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Contact")
                .param("email", "contact@example.com")
                .param("phoneNumber", "1234567890")
                .param("street", "123 Main St")
                .param("city", "Test City")
                .param("state", "Test State")
                .param("country", "Test Country")
                .param("zipCode", "12345")
                .param("favorite", "true")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user/contacts/view"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldSearchContacts() throws Exception {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.searchByName(eq("Test"), eq(10), eq(0), any(), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/search")
                .param("keyword", "Test")
                .param("field", "name"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_contacts"))
            .andExpect(model().attributeExists("contactsPage"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDeleteContact() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getContactById(1L)).thenReturn(testContact);
        doNothing().when(contactService).deleteContact(1L);

        mockMvc.perform(delete("/user/contacts/delete/{id}", 1L)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user/contacts/view"));
    }

    // QR Code Generation Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGenerateQRCode() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getContactById(1L)).thenReturn(testContact);
        byte[] mockQrCode = new byte[] { 1, 2, 3, 4 };
        when(qrCodeGeneratorService.generateQRCodeFromContact(testContact, 250, 250)).thenReturn(mockQrCode);

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(mockQrCode));

        verify(qrCodeGeneratorService).generateQRCodeFromContact(testContact, 250, 250);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGenerateQRCodeWithCustomSize() throws Exception {
        when(contactService.getContactById(1L)).thenReturn(testContact);
        byte[] mockQrCode = new byte[] { 1, 2, 3, 4 };
        when(qrCodeGeneratorService.generateQRCodeFromContact(testContact, 500, 300)).thenReturn(mockQrCode);

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 1L)
                .param("width", "500")
                .param("height", "300"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(mockQrCode));

        verify(qrCodeGeneratorService).generateQRCodeFromContact(testContact, 500, 300);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldGenerateQRCodeWithEmptyFields() throws Exception {
        Contact emptyContact = new Contact();
        emptyContact.setId(1L);
        emptyContact.setName("Test Contact");
        emptyContact.setUser(testUser);
        
        when(contactService.getContactById(1L)).thenReturn(emptyContact);
        byte[] mockQrCode = new byte[] { 1, 2, 3, 4 };
        when(qrCodeGeneratorService.generateQRCodeFromContact(emptyContact, 250, 250)).thenReturn(mockQrCode);

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(mockQrCode));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleQRCodeGenerationError() throws Exception {
        when(contactService.getContactById(1L)).thenReturn(testContact);
        when(qrCodeGeneratorService.generateQRCodeFromContact(any(), any(Integer.class), any(Integer.class)))
            .thenThrow(new RuntimeException("QR code generation failed"));

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 1L))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleInvalidSizeParameters() throws Exception {
        when(contactService.getContactById(1L)).thenReturn(testContact);

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 1L)
                .param("width", "-100")
                .param("height", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleContactNotFoundForQRCode() throws Exception {
        when(contactService.getContactById(999L))
            .thenThrow(new ResourceNotFoundException("Contact not found"));

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Contact not found"));
    }

    // QR Code Decoding Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDecodeQRCode() throws Exception {
        String mockJson = "{\"name\":\"Test Contact\",\"email\":\"test@example.com\",\"phoneNumber\":\"1234567890\"}";
        
        MockMultipartFile qrFile = new MockMultipartFile(
            "file",
            "qr.png",
            "image/png",
            com.scm.contactmanager.helper.QRCodeGenerator.generateQRCodeFromString(mockJson, 250, 250)
        );

        mockMvc.perform(multipart("/user/contacts/decode-qr")
                .file(qrFile)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.name").value("Test Contact"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.phoneNumber").value("1234567890"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleInvalidQRCodeData() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "invalid.png",
            "image/png",
            "Not a valid QR code".getBytes()
        );

        mockMvc.perform(multipart("/user/contacts/decode-qr")
                .file(invalidFile)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleInvalidQRCodeJSON() throws Exception {
        String invalidJson = "Not a valid JSON string";
        
        MockMultipartFile invalidJsonFile = new MockMultipartFile(
            "file",
            "invalid.png",
            "image/png",
            com.scm.contactmanager.helper.QRCodeGenerator.generateQRCodeFromString(invalidJson, 250, 250)
        );

        mockMvc.perform(multipart("/user/contacts/decode-qr")
                .file(invalidJsonFile)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.error").value(containsString("QR code does not contain valid contact data")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleQRCodeWithMaximumSize() throws Exception {
        when(contactService.getContactById(1L)).thenReturn(testContact);
        byte[] mockQrCode = new byte[] { 1, 2, 3, 4 };
        when(qrCodeGeneratorService.generateQRCodeFromContact(testContact, 1000, 1000)).thenReturn(mockQrCode);

        mockMvc.perform(get("/user/contacts/qrcode/{id}", 1L)
                .param("width", "1000")
                .param("height", "1000"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(mockQrCode));

        verify(qrCodeGeneratorService).generateQRCodeFromContact(testContact, 1000, 1000);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleMissingQRCodeFile() throws Exception {
        mockMvc.perform(multipart("/user/contacts/decode-qr")
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleUnsupportedMediaType() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "This is not an image".getBytes()
        );

        mockMvc.perform(multipart("/user/contacts/decode-qr")
                .file(invalidFile)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleEmptyQRCodeFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.png",
            "image/png",
            new byte[0]
        );

        mockMvc.perform(multipart("/user/contacts/decode-qr")
                .file(emptyFile)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandlePagination() throws Exception {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact), 
            PageRequest.of(1, 10), 20);
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getByUser(any(), eq(1), eq(10), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/view")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_contacts"))
            .andExpect(model().attribute("contactsPage", hasProperty("number", is(1))))
            .andExpect(model().attribute("contactsPage", hasProperty("size", is(10))));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUpdateContact() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getContactById(1L)).thenReturn(testContact);
        when(contactService.updateContact(any(Contact.class))).thenReturn(testContact);

        mockMvc.perform(post("/user/contacts/update_contact/{contactId}", 1L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Updated Contact")
                .param("email", "updated@example.com")
                .param("phoneNumber", "9876543210")
                .param("street", "456 Update St")
                .param("city", "Update City")
                .param("state", "Update State")
                .param("country", "Update Country")
                .param("zipCode", "54321")
                .param("favorite", "true")
                .param("description", "Updated description")
                .param("linkedInLink", "https://linkedin.com/updated")
                .param("websiteLink", "https://updated.com")
                .param("relationship", "FRIEND")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user/contacts/view"));

        verify(contactService, times(1)).updateContact(any(Contact.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleUpdateValidationErrors() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getContactById(1L)).thenReturn(testContact);

        mockMvc.perform(multipart("/user/contacts/update_contact/{contactId}", 1L)
                // The .contentType() line has been removed
                //.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "") // Empty name should cause validation error
                .param("email", "invalid-email") // Invalid email format
                .param("phoneNumber", "9876543210")
                .param("street", "456 Update St")
                .param("city", "Update City")
                .param("state", "Update State")
                .param("country", "Update Country")
                .param("zipCode", "54321")
                .param("favorite", "true")
                .param("description", "Updated description")
                .param("linkedInLink", "https://linkedin.com/updated")
                .param("websiteLink", "https://updated.com")
                .param("relationship", "FRIEND")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("user/edit_contact"))
            .andExpect(model().attributeHasFieldErrors("contactForm", "name"))
            .andExpect(model().attributeHasFieldErrors("contactForm", "email"));
    }


    @Test
    @WithMockUser(username = "test@example.com")
    void shouldSearchContactsByEmail() throws Exception {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.searchByEmail(eq("test@"), eq(10), eq(0), any(), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/search")
                .param("keyword", "test@")
                .param("field", "email"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_contacts"))
            .andExpect(model().attributeExists("contactsPage"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldSearchContactsByPhone() throws Exception {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.searchByPhoneNumber(eq("123"), eq(10), eq(0), any(), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/search")
                .param("keyword", "123")
                .param("field", "phone"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_contacts"))
            .andExpect(model().attributeExists("contactsPage"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldSearchContactsByRelationship() throws Exception {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.searchByRelationship(eq("FRIEND"), eq(10), eq(0), any(), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(get("/user/contacts/search")
                .param("keyword", "FRIEND")
                .param("field", "relationship"))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_contacts"))
            .andExpect(model().attributeExists("contactsPage"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldSearchFavoriteContactsByName() throws Exception {
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(testContact));
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.searchFavoriteByName(eq("Test"), eq(10), eq(0), any(), any(), any()))
            .thenReturn(contactPage);

        mockMvc.perform(post("/user/contacts/favorites")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("keyword", "Test")
                .param("field", "name")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("user/view_favorite_contacts"))
            .andExpect(model().attributeExists("contactsPage"))
            .andExpect(model().attribute("favoriteView", true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUploadContactImage() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
        when(contactService.getContactById(1L)).thenReturn(testContact);
        when(contactService.updateContact(any(Contact.class))).thenReturn(testContact);
        when(imageService.uploadImage(any(), any())).thenReturn("http://example.com/image.jpg");

        MockMultipartFile imageFile = new MockMultipartFile(
            "contactImage",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        mockMvc.perform(multipart("/user/contacts/update_contact/{contactId}", 1L)
                .file(imageFile)
                .param("name", "Test Contact")
                .param("email", "contact@example.com")
                .param("phoneNumber", "1234567890")
                .param("street", "123 Main St")
                .param("city", "Test City")
                .param("state", "Test State")
                .param("country", "Test Country")
                .param("zipCode", "12345")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/user/contacts/view"));

        verify(imageService, times(1)).uploadImage(any(), any());
    }
}
