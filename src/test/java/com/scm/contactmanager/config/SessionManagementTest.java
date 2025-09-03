package com.scm.contactmanager.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SessionManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectToLoginWhenSessionExpires() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // First request with valid session
        mockMvc.perform(get("/user/contacts/view").session(session))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));

        // Invalidate session
        session.invalidate();

        // Request after session expiration
        mockMvc.perform(get("/user/contacts/view").session(session))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldMaintainSessionBetweenRequests() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // First request
        MvcResult result = mockMvc.perform(get("/user/contacts/view").session(session))
                                 .andExpect(status().isOk())
                                 .andReturn();

        // Subsequent request with same session
        mockMvc.perform(get("/user/contacts/view").session(session))
               .andExpect(status().isOk());

        // Verify session attributes are maintained
        assertNotNull(session.getAttribute("user"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldHandleConcurrentSessions() throws Exception {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();

        // First session
        mockMvc.perform(get("/user/contacts/view").session(session1))
               .andExpect(status().isOk());

        // Second session (should invalidate first session if maxSessions=1)
        mockMvc.perform(get("/user/contacts/view").session(session2))
               .andExpect(status().isOk());

        // First session should be invalid
        mockMvc.perform(get("/user/contacts/view").session(session1))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void shouldHandleSessionTimeout() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // Initial request
        mockMvc.perform(get("/user/contacts/view").session(session))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));

        // Simulate session timeout
        Thread.sleep(1000); // Adjust based on your session timeout settings
        session.setMaxInactiveInterval(1);

        // Request after timeout
        mockMvc.perform(get("/user/contacts/view").session(session))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReissueSessionOnLogin() throws Exception {
        MockHttpSession oldSession = new MockHttpSession();

        // Pre-login request
        mockMvc.perform(get("/user/contacts/view").session(oldSession))
               .andExpect(status().isOk());

        String oldSessionId = oldSession.getId();

        // Login request (should create new session)
        MvcResult loginResult = mockMvc.perform(post("/login")
                .param("username", "test@example.com")
                .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession newSession = (MockHttpSession) loginResult.getRequest().getSession();
        assertNotEquals(oldSessionId, newSession.getId());
    }
}
