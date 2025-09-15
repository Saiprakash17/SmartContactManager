package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
@EnableWebMvc
@ComponentScan(basePackages = {
    "com.scm.contactmanager.controllers",
    "com.scm.contactmanager.services",
    "com.scm.contactmanager.helper"
})
@Import({
    TestSecurityConfig.class,
    TestMailConfig.class,
    TestDataSourceConfig.class
})
@Profile("test")
public class TestConfig implements WebMvcConfigurer {
    // Configuration for WebMvc in tests
}
