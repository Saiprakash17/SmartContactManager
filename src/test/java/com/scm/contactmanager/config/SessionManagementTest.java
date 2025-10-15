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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;



@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
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

    @BeforeEach
    void setUp() {
       // Set up mock user in UserRepo for SecurityCustomUserDeatilsService
       com.scm.contactmanager.entities.User user = com.scm.contactmanager.entities.User.builder()
              .name("Test User") // Also add a name to the user object
              .email("test@example.com")
              .password(new BCryptPasswordEncoder().encode("testpassword"))
              .roles(List.of("ROLE_USER"))
              .enabled(true)
              .build();
       when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

       // Add this line to configure the userService mock
       when(userService.getUserByEmail("test@example.com")).thenReturn(user);
    }

    @Test
    @WithAnonymousUser
    void shouldRedirectToLoginWhenSessionExpires() throws Exception {
        mockMvc.perform(get("/user/dashboard")
               .with(csrf())
               .with(request -> {
                   request.setRequestedSessionIdValid(false);
                   return request;
               }))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
    void shouldMaintainSessionBetweenRequests() throws Exception {
        MockHttpSession session = new MockHttpSession();
        
        // Perform login first
        mockMvc.perform(post("/authenticate")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isOk());

        // First request to dashboard with authenticated session
        mockMvc.perform(get("/user/dashboard")
                .session(session)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void shouldHandleConcurrentSessions() throws Exception {
        // First login
        MockHttpSession session1 = new MockHttpSession();
        mockMvc.perform(post("/authenticate")
                .session(session1)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/user/dashboard"));

        // Second login with the same user should be prevented with error
        MockHttpSession session2 = new MockHttpSession();
        mockMvc.perform(post("/authenticate")
                .session(session2)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void shouldHandleSessionTimeout() throws Exception {
        // Create a new session
        MockHttpSession session = new MockHttpSession();

        // First request - should succeed
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("http://localhost/login"));

        // Invalidate the session to simulate timeout
        session.invalidate();

        // Try accessing with expired session
        mockMvc.perform(get("/user/dashboard")
               .with(request -> {
                   request.setRequestedSessionIdValid(false);
                   request.setRequestedSessionId(session.getId());
                   return request;
               })
               .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("http://localhost/login?expired=true"));
    }

    @Test
    @WithAnonymousUser
    void shouldReissueSessionOnLogin() throws Exception {
        // Create initial session
        MockHttpSession session = new MockHttpSession();

        // First try accessing protected resource without auth
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("http://localhost/login"));

        // Then perform login with the same session
        MvcResult result = mockMvc.perform(post("/authenticate")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost/login"))
                .andReturn();

        // Verify session cookie exists
        assertNotNull(result.getResponse().getCookie("JSESSIONID"), 
                     "Session cookie should be created after login");

        // Try accessing a protected resource with the new session
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .cookie(result.getResponse().getCookies())
               .with(csrf()))
               .andExpect(status().isFound())
               .andExpect(redirectedUrl("http://localhost/login"));
    }
}
