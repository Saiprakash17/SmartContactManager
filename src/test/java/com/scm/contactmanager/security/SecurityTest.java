package com.scm.contactmanager.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.scm.contactmanager.config.TestSecurityConfig;
import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.UserService;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ContactService contactService;

    @BeforeEach
    void setup() {
        User testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        testUser.setPassword("$2a$10$aMuOvpLPRmtxqWIqdpxKze23fTe6WW/3w.dlzXXuoGm3UjRXDXsYe"); // "testpassword" hashed
        testUser.setName("Test User");
        testUser.setRoles(Collections.singletonList("ROLE_USER"));
        
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        Page<Contact> emptyPage = new PageImpl<>(Collections.emptyList());
        when(contactService.getByUser(any(), any(Integer.class), any(Integer.class), any(), any()))
            .thenReturn(emptyPage);
    }

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
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldAcceptRequestsWithCsrf() throws Exception {
        mockMvc.perform(post("/user/contacts/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Contact")
                .param("email", "contact@example.com")
                .param("phoneNumber", "1234567890")
                .param("street", "123 Test St")
                .param("city", "Test City")
                .param("state", "Test State")
                .param("country", "Test Country")
                .param("zipCode", "12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/contacts/view"));
    }
}
