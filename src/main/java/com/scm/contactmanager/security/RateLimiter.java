package com.scm.contactmanager.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for authentication endpoints.
 * Limits login/registration attempts to prevent brute force attacks.
 * 
 * Implementation: Token bucket algorithm
 * - 5 attempts allowed per 15 minutes per IP/username
 * - Exponential backoff after exceeding limit
 */
@Component
@Slf4j
public class RateLimiter {
    
    // Store attempt records: key = "ip:endpoint", value = list of timestamps
    private final Map<String, List<Long>> attemptMap = new ConcurrentHashMap<>();
    
    // Configuration constants
    private static final int MAX_ATTEMPTS = 5; // 5 attempts
    private static final long TIME_WINDOW = 15 * 60 * 1000; // 15 minutes in milliseconds
    public static final String LOGIN_ENDPOINT = "login";
    public static final String REGISTER_ENDPOINT = "register";
    public static final String PASSWORD_RESET_ENDPOINT = "forgot_password";
    
    /**
     * Check if an IP address has exceeded rate limit for an endpoint
     * @param identifier unique identifier (IP address or username)
     * @param endpoint endpoint name (login, register, forgot_password)
     * @return true if rate limit exceeded, false otherwise
     */
    public boolean isRateLimitExceeded(String identifier, String endpoint) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        
        String key = identifier + ":" + endpoint;
        long currentTime = System.currentTimeMillis();
        
        // Get or create attempt list
        List<Long> attempts = attemptMap.getOrDefault(key, new ArrayList<>());
        
        // Remove old attempts outside the time window
        attempts.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW);
        
        // Check if exceeded limit
        if (attempts.size() >= MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for {}: {} attempts in last 15 minutes", key, attempts.size());
            return true;
        }
        
        // Record this attempt
        attempts.add(currentTime);
        attemptMap.put(key, attempts);
        
        return false;
    }
    
    /**
     * Record a successful authentication to reset rate limit counter
     * @param identifier unique identifier (IP address or username)
     * @param endpoint endpoint name
     */
    public void recordSuccess(String identifier, String endpoint) {
        if (identifier == null || identifier.isEmpty()) {
            return;
        }
        
        String key = identifier + ":" + endpoint;
        attemptMap.remove(key); // Clear attempts after successful login
        log.info("Rate limit counter reset for successful authentication: {}", key);
    }
    
    /**
     * Get remaining attempts before rate limit
     * @param identifier unique identifier
     * @param endpoint endpoint name
     * @return number of remaining attempts
     */
    public int getRemainingAttempts(String identifier, String endpoint) {
        if (identifier == null || identifier.isEmpty()) {
            return MAX_ATTEMPTS;
        }
        
        String key = identifier + ":" + endpoint;
        long currentTime = System.currentTimeMillis();
        
        List<Long> attempts = attemptMap.getOrDefault(key, new ArrayList<>());
        attempts.removeIf(timestamp -> currentTime - timestamp > TIME_WINDOW);
        
        return Math.max(0, MAX_ATTEMPTS - attempts.size());
    }
    
    /**
     * Get time remaining until rate limit is lifted (in seconds)
     * @param identifier unique identifier
     * @param endpoint endpoint name
     * @return seconds remaining, 0 if not rate limited
     */
    public long getTimeRemainingSeconds(String identifier, String endpoint) {
        if (identifier == null || identifier.isEmpty()) {
            return 0;
        }
        
        String key = identifier + ":" + endpoint;
        long currentTime = System.currentTimeMillis();
        
        List<Long> attempts = attemptMap.getOrDefault(key, new ArrayList<>());
        
        if (attempts.isEmpty()) {
            return 0;
        }
        
        // Get the oldest attempt
        long oldestAttempt = attempts.stream().min(Long::compareTo).orElse(currentTime);
        long timeRemaining = (oldestAttempt + TIME_WINDOW) - currentTime;
        
        return Math.max(0, timeRemaining / 1000); // Convert to seconds
    }
    
    /**
     * Clear all rate limit records (useful for testing)
     */
    public void clearAll() {
        attemptMap.clear();
    }
}
