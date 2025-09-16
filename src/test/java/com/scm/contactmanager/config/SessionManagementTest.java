package com.scm.contactmanager.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

import com.scm.contactmanager.controllers.UserController;
import com.scm.contactmanager.controllers.PageController;
import com.scm.contactmanager.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;



@WebMvcTest(controllers = {UserController.class, PageController.class})
@Import({SecurityConfig.class, TestSecurityConfig.class})
class SessionManagementTest {

    @MockBean
    private UserService userService;
    
    @MockBean
    private com.scm.contactmanager.repositories.UserRepo userRepo;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Set up mock user in UserRepo for SecurityCustomUserDeatilsService
        com.scm.contactmanager.entities.User user = com.scm.contactmanager.entities.User.builder()
            .email("test@example.com")
            .password(new BCryptPasswordEncoder().encode("testpassword"))
            .roles(List.of("ROLE_USER"))
            .enabled(true)
            .build();
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void shouldRedirectToLoginWhenSessionExpires() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // First request with invalid session should redirect to login
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));

        // Invalidate session
        session.invalidate();

        // Request after session expiration should also redirect
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
    void shouldMaintainSessionBetweenRequests() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // First request to dashboard with authenticated session
        mockMvc.perform(get("/user/dashboard")
                .session(session)
                .with(csrf()))
                .andExpect(status().isOk());

        // Second request with same session should work
        mockMvc.perform(get("/user/dashboard")
                .session(session)
                .with(csrf()))
                .andExpect(status().isOk());
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
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        // Second login with the same user should fail
        MockHttpSession session2 = new MockHttpSession();
        mockMvc.perform(post("/authenticate")
                .session(session2)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void shouldHandleSessionTimeout() throws Exception {
        // First login to create a valid session
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/authenticate")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        // Simulate session timeout
        session.setMaxInactiveInterval(1);
        Thread.sleep(1100); // Wait longer than the timeout

        // Request after timeout should redirect to login
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void shouldReissueSessionOnLogin() throws Exception {
        // First create a session
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(get("/user/dashboard")
               .session(session)
               .with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));

        String oldSessionId = session.getId();

        // Then login with same session
        mockMvc.perform(post("/authenticate")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@example.com")
                .param("password", "testpassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"));

        assertNotEquals(oldSessionId, session.getId());
    }
}
