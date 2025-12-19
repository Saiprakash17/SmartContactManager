package com.scm.contactmanager.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Security audit logging component for tracking sensitive operations.
 * Logs authentication events, file uploads, password changes, and admin actions
 * for security monitoring and compliance audit trails.
 */
@Component
@Slf4j
public class SecurityAuditLogger {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Audit event types for categorization
     */
    public enum AuditEventType {
        LOGIN("USER_LOGIN"),
        FAILED_LOGIN("FAILED_LOGIN"),
        LOGOUT("USER_LOGOUT"),
        REGISTER("USER_REGISTER"),
        PASSWORD_CHANGE("PASSWORD_CHANGE"),
        PASSWORD_RESET("PASSWORD_RESET"),
        FILE_UPLOAD("FILE_UPLOAD"),
        FILE_DELETE("FILE_DELETE"),
        ADMIN_ACTION("ADMIN_ACTION"),
        UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS"),
        INVALID_INPUT("INVALID_INPUT");
        
        private final String eventName;
        
        AuditEventType(String eventName) {
            this.eventName = eventName;
        }
        
        public String getEventName() {
            return eventName;
        }
    }
    
    /**
     * Log a security audit event
     * @param eventType type of security event
     * @param username username of the user performing the action
     * @param details additional details about the event
     */
    public void logAuditEvent(AuditEventType eventType, String username, String details) {
        String ipAddress = getClientIPAddress();
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        
        String auditLog = String.format(
            "[SECURITY_AUDIT] %s | TYPE=%s | USER=%s | IP=%s | DETAILS=%s",
            timestamp,
            eventType.getEventName(),
            sanitize(username),
            ipAddress,
            sanitize(details)
        );
        
        log.warn(auditLog); // Use WARN level for all audit events for visibility
    }
    
    /**
     * Log a successful login
     * @param username username
     */
    public void logLogin(String username) {
        logAuditEvent(AuditEventType.LOGIN, username, "Successful authentication");
    }
    
    /**
     * Log a failed login attempt
     * @param username username attempted
     * @param reason reason for failure
     */
    public void logFailedLogin(String username, String reason) {
        logAuditEvent(AuditEventType.FAILED_LOGIN, username, "Reason: " + reason);
    }
    
    /**
     * Log user logout
     * @param username username
     */
    public void logLogout(String username) {
        logAuditEvent(AuditEventType.LOGOUT, username, "User logged out");
    }
    
    /**
     * Log user registration
     * @param username username registered
     * @param email email registered
     */
    public void logRegistration(String username, String email) {
        logAuditEvent(AuditEventType.REGISTER, username, "Registration with email: " + sanitize(email));
    }
    
    /**
     * Log password change
     * @param username username
     */
    public void logPasswordChange(String username) {
        logAuditEvent(AuditEventType.PASSWORD_CHANGE, username, "Password changed successfully");
    }
    
    /**
     * Log password reset request
     * @param email email address
     */
    public void logPasswordReset(String email) {
        logAuditEvent(AuditEventType.PASSWORD_RESET, "unknown", "Password reset requested for: " + sanitize(email));
    }
    
    /**
     * Log file upload
     * @param username username uploading file
     * @param filename name of the file uploaded
     * @param fileSize size of the file in bytes
     * @param fileType MIME type of the file
     */
    public void logFileUpload(String username, String filename, long fileSize, String fileType) {
        String details = String.format(
            "File uploaded - Name: %s, Size: %d bytes, Type: %s",
            sanitize(filename),
            fileSize,
            fileType
        );
        logAuditEvent(AuditEventType.FILE_UPLOAD, username, details);
    }
    
    /**
     * Log file deletion
     * @param username username deleting file
     * @param filename name of the file deleted
     */
    public void logFileDelete(String username, String filename) {
        logAuditEvent(AuditEventType.FILE_DELETE, username, "File deleted: " + sanitize(filename));
    }
    
    /**
     * Log admin action
     * @param adminUsername admin performing the action
     * @param action description of the admin action
     * @param targetUser target user affected by the action (if applicable)
     */
    public void logAdminAction(String adminUsername, String action, String targetUser) {
        String details = targetUser != null ? 
            action + " | Target User: " + sanitize(targetUser) : 
            action;
        logAuditEvent(AuditEventType.ADMIN_ACTION, adminUsername, details);
    }
    
    /**
     * Log unauthorized access attempt
     * @param username username attempting access
     * @param resource resource being accessed
     * @param reason reason access was denied
     */
    public void logUnauthorizedAccess(String username, String resource, String reason) {
        String details = "Resource: " + resource + " | Reason: " + reason;
        logAuditEvent(AuditEventType.UNAUTHORIZED_ACCESS, username, details);
    }
    
    /**
     * Log invalid input detection
     * @param username username submitting invalid input
     * @param fieldName name of the field with invalid input
     * @param reason validation error reason
     */
    public void logInvalidInput(String username, String fieldName, String reason) {
        String details = "Field: " + fieldName + " | Error: " + reason;
        logAuditEvent(AuditEventType.INVALID_INPUT, username, details);
    }
    
    /**
     * Get client IP address from the current request
     * @return client IP address or "unknown" if unavailable
     */
    private String getClientIPAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // Check for X-Forwarded-For header (for proxied requests)
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                
                // Check for X-Real-IP header
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                
                // Fallback to remote address
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not determine client IP address", e);
        }
        return "unknown";
    }
    
    /**
     * Sanitize sensitive data before logging (prevent PII exposure in logs)
     * @param data data to sanitize
     * @return sanitized data
     */
    private String sanitize(String data) {
        if (data == null || data.isEmpty()) {
            return "N/A";
        }
        
        // Limit log message length to prevent log injection
        if (data.length() > 100) {
            return data.substring(0, 97) + "...";
        }
        
        return data;
    }
}
