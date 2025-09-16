package com.scm.contactmanager.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.scm.contactmanager.controllers.PageController;
import com.scm.contactmanager.controllers.UserController;
import com.scm.contactmanager.controllers.ContactController;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.repositories.UserRepo;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.PasswordResetTokenService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.impl.SecurityCustomUserDeatilsService;

@WebMvcTest(controllers = {UserController.class, PageController.class, ContactController.class})
@Import({TestSecurityConfig.class})
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private ContactService contactService;

    @MockBean
    private UserRepo userRepo;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordResetTokenService passwordResetTokenService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private QRCodeGeneratorService qrCodeGeneratorService;

    @MockBean
    private SecurityCustomUserDeatilsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create test user
        testUser = User.builder()
            .id("test-user-id")
            .email("test@example.com")
            .password(new BCryptPasswordEncoder().encode("testpassword"))
            .name("Test User")
            .roles(List.of("ROLE_USER"))
            .enabled(true)
            .build();

        // Configure mocks
        when(userService.getUserByEmail(anyString())).thenReturn(testUser);
        when(userService.getUserById(anyString())).thenReturn(Optional.of(testUser));
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
        
        // Configure contact service mocks
        when(contactService.getAllContacts()).thenReturn(List.of());
        when(contactService.getByUser(any(User.class), anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(new PageImpl<>(List.of()));
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
