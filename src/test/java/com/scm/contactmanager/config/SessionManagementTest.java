package com.scm.contactmanager.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.scm.contactmanager.controllers.ApiController;
import com.scm.contactmanager.controllers.ContactController;
import com.scm.contactmanager.controllers.PageController;
import com.scm.contactmanager.services.AddressService;
import com.scm.contactmanager.services.ApiService;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.ImageService;
import com.scm.contactmanager.services.PageService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.scm.contactmanager.services.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PageController.class, ApiController.class, ContactController.class})
@Import({TestSecurityConfig.class, CommonTestConfig.class})
@ActiveProfiles("test")
public class SessionManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRegistry sessionRegistry;

    @MockBean
    private UserService userService;

    @MockBean
    private ContactService contactService;

    @MockBean
    private AddressService addressService;

    @MockBean
    private ApiService apiService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private QRCodeGeneratorService qrCodeGeneratorService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private PageService pageService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_RAW_PASSWORD = "testpassword";

    @BeforeEach
    void setUp() {
        sessionRegistry.getAllPrincipals().forEach(principal -> {
            sessionRegistry.getAllSessions(principal, false)
                    .forEach(session -> sessionRegistry.removeSessionInformation(session.getSessionId()));
        });
    }

    @Test
    void givenValidCredentials_whenLogin_thenSessionIsCreated() throws Exception {
        MvcResult result = mockMvc.perform(formLogin("/authenticate")
                        .user("email", TEST_EMAIL)
                        .password("password", TEST_RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        assertNotNull(session);
        assertNotNull(sessionRegistry.getSessionInformation(session.getId()));
    }

    @Test
    void givenMaximumSessionsExceeded_whenLogin_thenDenyAccess() throws Exception {
        // First login
        mockMvc.perform(formLogin("/authenticate")
                        .user("email", TEST_EMAIL)
                        .password("password", TEST_RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andExpect(authenticated().withUsername(TEST_EMAIL))
                .andReturn();

        // Try second login - should be rejected
        mockMvc.perform(formLogin("/authenticate")
                        .user("email", TEST_EMAIL)
                        .password("password", TEST_RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    void givenValidSession_whenSessionExpires_thenRedirectToLogin() throws Exception {
        // First login
        MvcResult result = mockMvc.perform(formLogin("/authenticate")
                        .user("email", TEST_EMAIL)
                        .password("password", TEST_RAW_PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/dashboard"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        assertNotNull(session, "Session should not be null");

        // Expire the session
        session.setMaxInactiveInterval(0);
        session.invalidate();

        // Try accessing protected resource with expired session
        mockMvc.perform(get("/user/dashboard")
                        .session(session)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void givenInvalidSession_whenAccessingProtectedResource_thenRedirectToLogin() throws Exception {
        // Create an invalid session ID
        MockHttpSession invalidSession = new MockHttpSession();
        invalidSession.invalidate();

        mockMvc.perform(get("/user/dashboard")
                        .session(invalidSession)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}