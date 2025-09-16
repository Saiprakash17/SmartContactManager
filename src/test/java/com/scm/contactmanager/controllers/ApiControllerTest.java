package com.scm.contactmanager.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.scm.contactmanager.config.TestConfig;
import com.scm.contactmanager.config.TestSecurityConfig;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.config.GlobalExceptionHandler;

@WebMvcTest(ApiController.class)
@Import({TestConfig.class, TestSecurityConfig.class, GlobalExceptionHandler.class})
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepo userRepo;

    private Contact testContact;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setId(1L);
        testContact.setName("Test Contact");
        testContact.setEmail("test@example.com");
    }

    @Test
    void shouldReturnContactWhenValidId() throws Exception {
        when(contactService.getContactById(1L)).thenReturn(testContact);

        mockMvc.perform(get("/api/contact/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Test Contact"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void shouldReturn404WhenContactNotFound() throws Exception {
        when(contactService.getContactById(anyLong()))
            .thenThrow(new ResourceNotFoundException("Contact", "id", "999"));

        mockMvc.perform(get("/api/contact/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Not found"))
            .andExpect(jsonPath("$.message").value("Contact not found with id : '999'"));
    }

    @Test
    void shouldReturn400WhenInvalidId() throws Exception {
        mockMvc.perform(get("/api/contact/invalid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid format"))
            .andExpect(jsonPath("$.message").value("Contact ID must be a valid number"));
    }

    @Test
    void shouldReturn400WhenIdIsEmpty() throws Exception {
        mockMvc.perform(get("/api/contact/ "))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid input"))
            .andExpect(jsonPath("$.message").value("Contact ID cannot be null or empty"));
    }
}
