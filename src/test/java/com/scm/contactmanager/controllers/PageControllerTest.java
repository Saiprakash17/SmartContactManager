package com.scm.contactmanager.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import com.scm.contactmanager.config.CommonTestConfig;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.services.EmailService;
import com.scm.contactmanager.services.PageService;
import com.scm.contactmanager.services.PasswordResetTokenService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.repositories.UserRepo;

@WebMvcTest(PageController.class)
@Import({CommonTestConfig.class, com.scm.contactmanager.config.TestSecurityConfig.class})
public class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepo userRepo;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordResetTokenService passwordResetTokenService;

    @MockBean
    private PageService pageService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
    }

    @Test
    void shouldDisplayHomePage() throws Exception {
        mockMvc.perform(get("/home"))
            .andExpect(status().isOk())
            .andExpect(view().name("home"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attributeExists("message"));
    }

    @Test
    void shouldDisplayLoginPage() throws Exception {
        when(userService.getUserByEmail(any())).thenReturn(testUser);

        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    void shouldDisplayRegisterPage() throws Exception {
        when(userService.getUserByEmail(any())).thenReturn(testUser);

        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"))
            .andExpect(model().attributeExists("userForm"));
    }

    @Test
    void shouldDisplayAboutPage() throws Exception {
        mockMvc.perform(get("/about"))
            .andExpect(status().isOk())
            .andExpect(view().name("about"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attributeExists("message"));
    }

    @Test
    void shouldRedirectHomeOnIndex() throws Exception {
        when(userService.getUserByEmail(any())).thenReturn(testUser);

        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));
    }
}
