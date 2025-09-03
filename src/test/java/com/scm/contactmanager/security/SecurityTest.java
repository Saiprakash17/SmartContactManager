package com.scm.contactmanager.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.config.TestConfig;

@SpringBootTest(classes = {com.scm.contactmanager.ContactmanagerApplication.class, TestConfig.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("testuser1")
            .name("Security Test User")
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .roles(Arrays.asList("ROLE_USER"))
            .enabled(true)
            .build();

        try {
            if (!userService.isUserPresentByEmail(testUser.getEmail())) {
                userService.saveUser(testUser);
            }
        } catch (Exception e) {
            // User might already exist
        }
    }

    // CSRF Protection Tests
    @Test
    void shouldRejectRequestsWithoutCsrf() throws Exception {
        mockMvc.perform(post("/user/process-contact")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Contact")
                .param("email", "contact@example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldAcceptRequestsWithCsrf() throws Exception {
        mockMvc.perform(post("/user/add-contact")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Contact")
                .param("email", "contact@example.com"))
                .andExpect(status().is3xxRedirection());
    }

    // XSS Prevention Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void shouldEscapeXssInContactName() throws Exception {
        String maliciousScript = "<script>alert('xss')</script>";
        
        mockMvc.perform(post("/user/add-contact")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", maliciousScript)
                .param("email", "contact@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(not(containsString("<script>"))));
    }

    // SQL Injection Prevention Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void shouldPreventSqlInjectionInSearch() throws Exception {
        String sqlInjection = "'; DROP TABLE contacts; --";
        
        mockMvc.perform(get("/user/search/contacts")
                .param("keyword", sqlInjection)
                .param("field", "name"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors());
    }

    // Role-based Access Control Tests
    @Test
    void shouldDenyAccessToUnauthenticatedUsers() throws Exception {
        mockMvc.perform(get("/user/show-contacts/0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void shouldAllowAccessToAuthorizedUsers() throws Exception {
        mockMvc.perform(get("/user/show-contacts/0"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void shouldDenyAccessToOtherUsersContacts() throws Exception {
        mockMvc.perform(get("/user/contact/1"))
                .andExpect(status().isForbidden());
    }

    // Password Security Tests
    @Test
    void shouldStoreBcryptHashedPasswords() {
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        assertTrue(hashedPassword.startsWith("$2a$"));
        assertTrue(passwordEncoder.matches(rawPassword, hashedPassword));
        assertFalse(passwordEncoder.matches("wrongpassword", hashedPassword));
    }

    @Test
    void shouldPreventWeakPasswords() throws Exception {
        mockMvc.perform(post("/do_register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test User")
                .param("email", "test@example.com")
                .param("password", "123")) // Too short password
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("user", "password"));
    }

    // Additional Security Headers Tests
    @Test
    void shouldIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"));
    }

    // Brute Force Prevention Tests
    @Test
    void shouldLockAccountAfterFailedAttempts() throws Exception {
        String username = "test@example.com";
        String wrongPassword = "wrongpassword";

        // Attempt multiple failed logins
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/login")
                    .with(csrf())
                    .param("username", username)
                    .param("password", wrongPassword))
                    .andExpect(status().is3xxRedirection());
        }

        // Verify account is locked
        mockMvc.perform(post("/login")
                .with(csrf())
                .param("username", username)
                .param("password", "correctpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
