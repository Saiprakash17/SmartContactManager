package com.scm.contactmanager.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.scm.contactmanager.entities.Contact;
import com.scm.contactmanager.entities.User;
import com.scm.contactmanager.services.ContactService;
import com.scm.contactmanager.services.UserService;
import com.scm.contactmanager.services.QRCodeGeneratorService;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest {

    // 2. Register the GreenMail extension. It will start a server on a random free port.
    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testUser", "testPass"))
            .withPerMethodLifecycle(false);

    // 3. Use @DynamicPropertySource to get the random port from the extension
    //    and set it BEFORE the application starts. This solves the race condition.
    @DynamicPropertySource
    static void configureMailHost(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> greenMail.getSmtp().getBindTo());
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
    }


    @Autowired
    private UserService userService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private QRCodeGeneratorService qrCodeService;



    @Test
    @Sql("/test-data.sql")
    @Transactional
    void shouldPerformEndToEndContactOperations() {
    // Create user with unique email
    User user = new User();
    user.setName("Test User");
    user.setEmail("test_user_" + System.currentTimeMillis() + "@example.com");
    user.setPassword("password123");
    User savedUser = userService.saveUser(user);

        // Create contact
        Contact contact = new Contact();
        contact.setName("Test Contact");
        contact.setEmail("contact@example.com");
        contact.setPhoneNumber("1234567890");
        contact.setUser(savedUser);
        Contact savedContact = contactService.saveContact(contact);

        // Generate QR code
        try {
            byte[] qrCode = qrCodeService.generateQRCodeFromContact(savedContact, 250, 250);
            assertNotNull(qrCode);
        } catch (Exception e) {
            fail("QR code generation failed: " + e.getMessage());
        }

        // Update contact
        savedContact.setName("Updated Contact");
        Contact updatedContact = contactService.updateContact(savedContact);
        assertEquals("Updated Contact", updatedContact.getName());

        // Search contact
        assertTrue(contactService.searchByName("Updated", 10, 0, "name", "asc", savedUser)
                .getContent().contains(updatedContact));

        // Delete contact
        contactService.deleteContact(updatedContact.getId());
        assertTrue(contactService.getByUser(savedUser, 0, 10, "name", "asc").isEmpty());
    }

    @Test
    //@Transactional // Removed to allow commit
    void shouldHandleConcurrentOperations() throws InterruptedException {
    User user = new User();
    user.setName("Concurrent Test User");
    user.setEmail("concurrent_" + System.currentTimeMillis() + "@example.com");
    user.setPassword("password123");
    User savedUser = userService.saveUser(user);

        // Create multiple contacts concurrently
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                Contact contact = new Contact();
                contact.setName("Contact " + i);
                contact.setEmail("contact" + i + "@example.com");
                contact.setUser(savedUser);
                contactService.saveContact(contact);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 10; i < 20; i++) {
                Contact contact = new Contact();
                contact.setName("Contact " + i);
                contact.setEmail("contact" + i + "@example.com");
                contact.setUser(savedUser);
                contactService.saveContact(contact);
            }
        });

    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    long count = contactService.getByUser(savedUser, 0, 100, "name", "asc").getTotalElements();
    System.out.println("Concurrent contacts created: " + count);
    assertEquals(20, count);
    }

    @Test
    @Transactional
    void shouldHandleLargeDataSet() {
        User user = new User();
        user.setName("Large Data Test User");
        user.setEmail("large@example.com");
        user.setPassword("password123");
        User savedUser = userService.saveUser(user);

        // Create 1000 contacts
        for (int i = 0; i < 1000; i++) {
            Contact contact = new Contact();
            contact.setName("Contact " + i);
            contact.setEmail("contact" + i + "@example.com");
            contact.setUser(savedUser);
            contactService.saveContact(contact);
        }

        // Test pagination
        assertEquals(50, contactService.getByUser(savedUser, 0, 50, "name", "asc").getSize());
        assertEquals(1000, contactService.getByUser(savedUser, 0, 1000, "name", "asc").getTotalElements());

        // Test search performance
        long startTime = System.currentTimeMillis();
        contactService.searchByName("Contact", 10, 0, "name", "asc", savedUser);
        long endTime = System.currentTimeMillis();
        assertTrue((endTime - startTime) < 1000); // Search should complete within 1 second
    }
}
