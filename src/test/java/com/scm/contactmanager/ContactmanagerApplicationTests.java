package com.scm.contactmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.scm.contactmanager.services.EmailService;

@SpringBootTest
class ContactmanagerApplicationTests {

	@Test
	void contextLoads() {
	}

	// @Autowired
	// private EmailService emailService;
	// @Test
	// void sendEmail() {
	// 	emailService.sendEmail("saiprakash.bollam07@gmail.com", "Test", "This is a testing email");
	// }
}
