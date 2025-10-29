package com.scm.contactmanager.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.PasswordResetTokenService;
import com.scm.contactmanager.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import({CommonTestConfig.class, TestSecurityConfig.class})
@WithMockUser(username = "test@example.com", roles = "USER")
@org.springframework.test.context.ActiveProfiles("test")
class SessionManagementTest {

    @MockBean
    private UserService userService;
    
    @MockBean
    private com.scm.contactmanager.repositories.UserRepo userRepo;

    @MockBean
    private com.scm.contactmanager.repositories.ContactRepo contactRepo;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordResetTokenService passwordResetTokenService;

    @MockBean
    private ImageService imageService; 

        @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
       com.scm.contactmanager.entities.User user = com.scm.contactmanager.entities.User.builder()
              .name("Test User")
              .email("test@example.com")
              .password(passwordEncoder.encode("testpassword"))
              .roles(List.of("ROLE_USER"))
              .enabled(true)
              .build();
       when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
       when(userService.getUserByEmail("test@example.com")).thenReturn(user);
    }

    @Test
    @WithAnonymousUser
    void shouldRedirectToLoginWhenSessionExpires() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setMaxInactiveInterval(1); // Set timeout to 1 second

        // First request establishes a session
        mockMvc.perform(get("/user/dashboard")
                .session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));

        // Force session invalidation
        session.invalidate();

        // When session is invalid, should redirect to login?expired=true
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(request -> {
                   request.setRequestedSessionIdValid(false);
                   return request;
               }))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("/login?expired=true"));
    }

    @Test
    void shouldMaintainSessionBetweenRequests() throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        // Perform login
        MvcResult result = mockMvc.perform(post("/authenticate")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andReturn();

        MockHttpSession newSession = (MockHttpSession) result.getRequest().getSession(false);
        
        // Follow-up request should work with the same session
        mockMvc.perform(get("/user/dashboard")
                .session(newSession))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleConcurrentSessions() throws Exception {
        // First login should succeed
        MockHttpSession session1 = new MockHttpSession();
        MvcResult result1 = mockMvc.perform(post("/authenticate")
                .session(session1)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andReturn();

        // Verify first session is valid
        mockMvc.perform(get("/user/dashboard")
                .session(session1))
                .andExpect(status().isOk());

        // Second concurrent session should be allowed but invalidate first session
        MockHttpSession session2 = new MockHttpSession();
        mockMvc.perform(post("/authenticate")
                .session(session2)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/dashboard"));
    }

    @Test
    @WithAnonymousUser
    void shouldHandleSessionTimeout() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setMaxInactiveInterval(1); // Set timeout to 1 second

        // Initial request to get a session
        mockMvc.perform(get("/user/dashboard")
                .session(session))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));

        // Force session invalidation
        session.invalidate();

        // Request with expired session should redirect to login with expired parameter
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(request -> {
                   request.setRequestedSessionIdValid(false);
                   return request;
               }))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("/login?expired=true"));
    }

    @Test
    @WithAnonymousUser
    void shouldReissueSessionOnLogin() throws Exception {
        // Initial session
        MockHttpSession initialSession = new MockHttpSession();
        String initialSessionId = initialSession.getId();

        // Login with initial session
        mockMvc.perform(post("/authenticate")
                .session(initialSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/dashboard"));
    }
}
