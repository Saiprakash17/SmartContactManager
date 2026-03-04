package com.scm.contactmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT authentication.
 * Maps jwt.* properties from application.properties
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT secret key for signing tokens
     */
    private String secret = "your-secret-key-make-this-long-and-secure-in-production";

    /**
     * JWT token expiration time in milliseconds (default: 24 hours)
     */
    private long expiration = 86400000;

    public JwtProperties() {
    }

    public JwtProperties(String secret, long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
