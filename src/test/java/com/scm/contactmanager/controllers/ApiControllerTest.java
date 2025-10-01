package com.scm.contactmanager.controllers;


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

import com.scm.contactmanager.config.CommonTestConfig;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.helper.ResourceNotFoundException;
import com.scm.contactmanager.services.ApiService;
import com.scm.contactmanager.config.GlobalExceptionHandler;

@WebMvcTest(ApiController.class)
@Import({CommonTestConfig.class, GlobalExceptionHandler.class})
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiService apiService;

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
        when(apiService.getContact("1")).thenReturn(testContact);

        mockMvc.perform(get("/api/contact/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Test Contact"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void shouldReturn404WhenContactNotFound() throws Exception {
        when(apiService.getContact("999"))
            .thenThrow(new ResourceNotFoundException("Contact", "id", "999"));

        mockMvc.perform(get("/api/contact/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Not found"))
            .andExpect(jsonPath("$.message").value("Contact not found with id : '999'"));
    }

    @Test
    void shouldReturn400WhenInvalidId() throws Exception {
        when(apiService.getContact("invalid"))
            .thenThrow(new IllegalArgumentException("Contact ID must be a valid number"));

        mockMvc.perform(get("/api/contact/invalid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid format"))
            .andExpect(jsonPath("$.message").value("Contact ID must be a valid number"));
    }

    @Test
    void shouldReturn400WhenIdIsEmpty() throws Exception {
        when(apiService.getContact(" "))
            .thenThrow(new IllegalArgumentException("Contact ID cannot be null or empty"));

        mockMvc.perform(get("/api/contact/ "))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Invalid input"))
            .andExpect(jsonPath("$.message").value("Contact ID cannot be null or empty"));
    }
}
