package com.scm.contactmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
@TestPropertySource("classpath:application-test.properties")
public class TestAppConfig {

    @Bean
    @Primary
    public Cloudinary cloudinary() {
        return new Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", "test_cloud",
                "api_key", "test_key",
                "api_secret", "test_secret"
            )
        );
    }
}