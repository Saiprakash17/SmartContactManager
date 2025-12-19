# Security Features Developer Guide

## Quick Reference for Security Implementation

This document provides quick reference for developers on the security features implemented.

---

## 1. Content Security Policy (CSP)

**Purpose**: Prevent XSS attacks by controlling which resources can be loaded

**Location**: `SecurityConfig.java` (lines 80-110)

**Key Directive**: `script-src 'self' https://cdnjs.cloudflare.com https://cdn.jsdelivr.net https://unpkg.com`

**To Add New Resources**:
1. Update CSP directive in SecurityConfig.java
2. Test in browser console (F12) for violations
3. Only whitelist necessary origins

---

## 2. Rate Limiting

**Purpose**: Block brute force authentication attacks (5 attempts per 15 minutes per IP)

**Location**: `RateLimiter.java` - NEW component

**Protected Endpoints**:
- POST `/do-register` - Registration
- POST `/forgot-password` - Password reset

**Usage Example**:
```java
if (rateLimiter != null && rateLimiter.isRateLimitExceeded(clientIp, RateLimiter.REGISTER_ENDPOINT)) {
    long waitSeconds = rateLimiter.getTimeRemainingSeconds(clientIp, RateLimiter.REGISTER_ENDPOINT);
    // Block request and show wait time
}
```

**To Adjust Limits**: Edit constants in RateLimiter.java
```java
private static final int MAX_ATTEMPTS = 5;  // Change to desired limit
private static final long TIME_WINDOW = 15 * 60 * 1000;  // Change time window
```

---

## 3. Audit Logging

**Purpose**: Track all security events for compliance and investigation

**Location**: `SecurityAuditLogger.java` - NEW component

**Logged Events**: LOGIN, REGISTER, PASSWORD_CHANGE, FILE_UPLOAD, etc.

**Usage Example**:
```java
if (auditLogger != null) {
    auditLogger.logRegistration(username, email);
    auditLogger.logFileUpload(username, filename, size, mimeType);
    auditLogger.logPasswordChange(username);
}
```

**Log Output Format**:
```
[SECURITY_AUDIT] 2025-12-18 20:30:15 | TYPE=USER_LOGIN | USER=john_doe | IP=192.168.1.100 | DETAILS=...
```

---

## 4. Event Delegation (XSS Prevention)

**What**: Removed all inline onclick/onerror handlers (28 total)

**Implementation**: Event delegation in contacts.js

**Pattern**:
```html
<!-- Before -->
<button onclick="exportData()">Export</button>

<!-- After -->
<button class="export-btn">Export</button>
```

```javascript
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('export-btn')) {
        exportData();
    }
});
```

---

## 5. File Upload Validation

**Purpose**: Prevent malicious file uploads

**Validation**: Magic number checking + MIME-type verification

**Supported**: JPEG, PNG, GIF, PDF (up to 5MB)

**Code**: FileValidator.java (enhanced)

---

## 6. Pagination Security

**Purpose**: Prevent DoS via large requests

**Validation**: 
- page >= 0
- 1 <= size <= 50

**Endpoints**: All contact listing/search endpoints

---

## Test Status

**85 Total Tests**
- ✅ 84 Passing (98.8%)
- ❌ 1 Pre-existing Failure (shouldUploadContactImage)
- ✅ 0 New Failures from Security Changes

---

## Quick Troubleshooting

| Issue | Solution |
|-------|----------|
| CSP violations in console | Add source to CSP directive in SecurityConfig.java |
| Rate limiting not working | Check rateLimiter bean created, verify null check |
| Audit logs not appearing | Verify log level >= WARN in config |
| File upload failing | Check magic number matches file content, verify size < 5MB |

---

## More Information

- **Detailed Documentation**: See `RATE_LIMITING_AND_AUDIT_LOGGING.md`
- **Complete Summary**: See `SECURITY_IMPROVEMENTS_COMPLETE.md`
- **Audit Report**: See `SECURITY_QUICK_REFERENCE.md`

---

## Production Checklist

- [ ] Run full test suite: `./mvnw test`
- [ ] Verify no CSP violations in browser (F12)
- [ ] Test rate limiting (6+ auth attempts should block)
- [ ] Confirm audit logs in application output
- [ ] Configure log rotation for production
- [ ] Set up monitoring/alerting for security events
