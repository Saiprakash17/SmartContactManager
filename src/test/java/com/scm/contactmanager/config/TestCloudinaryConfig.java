package com.scm.contactmanager.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestCloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "test_cloud");
        config.put("api_key", "test_key");
        config.put("api_secret", "test_secret");
        return new Cloudinary(config);
    }
}
