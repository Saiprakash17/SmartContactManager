package com.scm.contactmanager.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;

import com.scm.contactmanager.config.TestSecurityConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({TestSecurityConfig.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class SecurityTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }





    @Test
    void shouldDenyAccessToUnauthenticatedUsers() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> {
                String location = result.getResponse().getHeader("Location");
                assertNotNull(location, "Expected a redirect Location header");
                assertTrue(location.endsWith("/login") || location.contains("/login"));
            });
    }

    @Test
    void shouldRejectRequestsWithoutCsrf() throws Exception {
        mockMvc.perform(post("/user/contacts/add")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("name", "Test Contact")
            .param("email", "contact@example.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> {
                String location = result.getResponse().getHeader("Location");
                assertNotNull(location, "Expected a redirect Location header");
                assertTrue(location.endsWith("/login") || location.contains("/login"));
            });
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldAllowAccessToAuthorizedUsers() throws Exception {
        mockMvc.perform(get("/user/dashboard"))
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
            .andExpect(redirectedUrl("/user/contacts/add"));
    }

    @Test
    void shouldSetSecurityHeaders() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void shouldHandleLoginFailure() throws Exception {
        mockMvc.perform(post("/authenticate")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", "wrong@example.com")
            .param("password", "wrongpassword"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldHandleLogout() throws Exception {
        // application may either redirect after logout or return 204 No Content. Accept both.
        MvcResult result = mockMvc.perform(post("/logout")
            .with(csrf()))
            .andReturn();

        int status = result.getResponse().getStatus();
        // either a redirect or successful/no-content
        assertTrue(status == 204 || (status >= 300 && status < 400));

        String location = result.getResponse().getHeader("Location");
        if (location != null) {
            assertTrue(location.contains("/login"));
        }

    jakarta.servlet.http.Cookie cookie = result.getResponse().getCookie("JSESSIONID");
    assertNotNull(cookie, "JSESSIONID cookie should be present or cleared");
    // When cookie exists, ensure max age indicates it's cleared (0)
    assertEquals(0, cookie.getMaxAge());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"USER", "ADMIN"})
    void shouldAllowAdminAccess() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void shouldDenyAdminAccessToUser() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldVerifyPasswordEncoding() {
        String rawPassword = "testPassword123";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        
        // Password should not be stored in plain text
        assert !encodedPassword.equals(rawPassword);
        
        // Password should be verifiable
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assert encoder.matches(rawPassword, encodedPassword);
    }
}
