package com.scm.contactmanager.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting filter to prevent API abuse and DoS attacks.
 * Limits API requests to 100 per minute per IP address.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final long MINUTE_IN_MS = 60 * 1000;
    
    // Map to track request counts per client IP
    private final Map<String, ClientRateLimit> clientLimits = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Only rate limit API endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = getClientKey(request);
        ClientRateLimit rateLimit = clientLimits.compute(clientKey, (key, existing) -> {
            if (existing == null) {
                return new ClientRateLimit();
            }
            // Reset if minute has elapsed
            if (System.currentTimeMillis() - existing.windowStart > MINUTE_IN_MS) {
                return new ClientRateLimit();
            }
            return existing;
        });

        if (rateLimit.increment() > MAX_REQUESTS_PER_MINUTE) {
            logger.warn("Rate limit exceeded for client: {} (requests: {})", clientKey, rateLimit.requestCount.get());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Rate limit exceeded. Max 100 requests per minute.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract client identifier for rate limiting.
     * Uses X-Forwarded-For header if available (for reverse proxies),
     * otherwise falls back to remote address.
     */
    private String getClientKey(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    /**
     * Inner class to track rate limiting per client
     */
    private static class ClientRateLimit {
        private final long windowStart = System.currentTimeMillis();
        private final AtomicInteger requestCount = new AtomicInteger(0);

        private int increment() {
            return requestCount.incrementAndGet();
        }
    }
}
