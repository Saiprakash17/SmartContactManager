package com.scm.contactmanager;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    // Common test utilities and setup can go here
}
