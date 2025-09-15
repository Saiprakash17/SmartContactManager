package com.scm.contactmanager.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.scm.contactmanager.config.TestConfig;
import com.scm.contactmanager.config.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDenyAccessToUnauthenticatedUsers() throws Exception {
        mockMvc.perform(get("/user/contacts/view"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void shouldRejectRequestsWithoutCsrf() throws Exception {
        mockMvc.perform(post("/user/contacts/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Contact")
                .param("email", "contact@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldAllowAccessToAuthorizedUsers() throws Exception {
        mockMvc.perform(get("/user/contacts/view"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("contactsPage"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldAcceptRequestsWithCsrf() throws Exception {
        mockMvc.perform(post("/user/contacts/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Contact")
                .param("email", "contact@example.com")
                .param("phoneNumber", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/contacts/view"));
    }
}
