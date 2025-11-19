# Security Fixes - Implementation Guide

## 🚀 Start Here: Critical Fixes (Do This First!)

---

## FIX #1: Remove Hardcoded Database Password ⭐ CRITICAL

**File:** `src/main/resources/application.properties` (Line 7)

**Current Code:**
```properties
spring.datasource.password=${MYSQL_PASSWORD:root1234}
```

**Problem:** 
- Default password `root1234` is exposed in code
- Anyone with access to repository can see production password
- Weak and guessable password

**Fixed Code:**
```properties
# Option 1: No default (strict, requires env var)
spring.datasource.password=${MYSQL_PASSWORD}

# Option 2: Fail with error message if not set
spring.datasource.password=${MYSQL_PASSWORD:?error.property.undefined}
```

**How to Apply:**
1. Update application.properties
2. Ensure `MYSQL_PASSWORD` environment variable is set
3. Test that application starts without hardcoded password
4. Delete password from git history:
   ```bash
   git log -p src/main/resources/application.properties | head -20
   # This shows password was committed, need to rewrite history!
   ```

---

## FIX #2: Replace ALL System.out with Logger ⭐ CRITICAL

**Files to Fix:**
1. `ImageServiceImpl.java`
2. `UserHelper.java`
3. `SessionHelper.java`
4. `DataSeeder.java`

### Fix ImageServiceImpl.java

**Current:**
```java
// Line 37-38
System.out.println("Error uploading image: " + e.getMessage());
e.printStackTrace();
```

**Fixed:**
```java
logger.error("Error uploading image", e);
```

**Complete Fixed Method:**
```java
@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String fileName) {
        try {
            byte[] data = new byte[file.getInputStream().available()];
            file.getInputStream().read(data);
            cloudinary.uploader().upload(data, ObjectUtils.asMap(
                "public_id", fileName
            ));
            return this.getUrlFromPublicId(fileName);
        } catch (IOException e) {
            logger.error("Error uploading image for file: {}", fileName, e);
            return null;
        }
    }
    // ... rest of class
}
```

### Fix UserHelper.java

**Current:**
```java
// Line 19, 24, 38
System.out.println("Getting email from google");
System.out.println("Getting email from github");
System.out.println("Getting email from normal login");
```

**Fixed:**
```java
public class UserHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(UserHelper.class);
    
    public static String getEmailOfLoggedInUser(Authentication authentication) {
        // ... 
        if (isOAuthProvider(authentication, "google")) {
            logger.debug("Extracting email from Google OAuth provider");
            // ...
        } else if (isOAuthProvider(authentication, "github")) {
            logger.debug("Extracting email from GitHub OAuth provider");
            // ...
        } else {
            logger.debug("Extracting email from normal login");
            // ...
        }
    }
}
```

### Fix SessionHelper.java

**Current:**
```java
// Line 14, 20, 23-24
System.out.println("removing message from session");
System.out.println("No request attributes found in SessionHelper");
System.out.println("Error in SessionHelper: " + e);
e.printStackTrace();
```

**Fixed:**
```java
public class SessionHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionHelper.class);
    
    public static void removeMessage(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                logger.debug("Removing message from session");
                session.removeAttribute("message");
            } else {
                logger.debug("No session found in request");
            }
        } catch (Exception e) {
            logger.error("Error removing message from session", e);
        }
    }
}
```

### Fix DataSeeder.java

**Current:**
```java
// Line 41
System.out.println("user created");
```

**Fixed:**
```java
private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

// In method
logger.info("User created successfully");
```

---

## FIX #3: Validate Sort Parameters - SQL Injection Prevention ⭐ CRITICAL

**File:** `src/main/java/com/scm/contactmanager/controllers/ContactController.java`

**Problem:**
User-controlled `sortBy` and `direction` parameters can be injected into JPA queries

**Fix - Add Parameter Validation:**

```java
@RequestMapping("/search")
public String SearchHandler(
    @ModelAttribute ContactsSearchForm contactSearchForm,
    @RequestParam(value = "size", defaultValue = AppConstants.PAGE_SIZE + "") int size,
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
    @RequestParam(value = "direction", defaultValue = "asc") String direction,
    Model model,
    Authentication authentication,
    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
) {
    // ADD THESE VALIDATIONS
    
    // 1. Validate sortBy - whitelist of allowed fields
    Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "name", 
        "email", 
        "phoneNumber", 
        "address",
        "about",
        "favorite",
        "createdAt",
        "updatedAt"
    );
    
    if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
        logger.warn("Invalid sort field attempted: {}", sortBy);
        redirectAttributes.addFlashAttribute("message",
            Message.builder()
                .content("Invalid sort field")
                .type(MessageType.red)
                .build());
        return "redirect:/user/contacts/view";
    }
    
    // 2. Validate direction - only asc or desc
    if (!direction.matches("^(asc|desc)$")) {
        logger.warn("Invalid sort direction attempted: {}", direction);
        redirectAttributes.addFlashAttribute("message",
            Message.builder()
                .content("Invalid sort direction")
                .type(MessageType.red)
                .build());
        return "redirect:/user/contacts/view";
    }
    
    // 3. Validate pagination parameters
    if (page < 0) {
        page = 0;
    }
    if (size < 1 || size > 100) {
        size = Integer.parseInt(AppConstants.PAGE_SIZE);
    }
    
    // Rest of method...
    User user = userService.getUserByEmail(UserHelper.getEmailOfLoggedInUser(authentication));
    
    if (contactSearchForm.getField() == null || contactSearchForm.getField().isEmpty() ||
        contactSearchForm.getKeyword() == null || contactSearchForm.getKeyword().isEmpty()) {
        redirectAttributes.addFlashAttribute("message",
            Message.builder()
                .content("Please select a field and enter a keyword to search")
                .type(MessageType.red)
                .build());
        return "redirect:/user/contacts/view";
    }

    try {
        Page<Contact> contactsPage = contactService.searchContacts(
            user, 
            contactSearchForm, 
            page, 
            size, 
            sortBy, 
            direction
        );
        // ... rest of logic
    }
    // ... error handling
}
```

---

## FIX #4: Implement Secure File Upload Validation 🟠 HIGH

**File:** `src/main/java/com/scm/contactmanager/validators/FileValidator.java`

**Current Code (Weak):**
```java
public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    if (file == null || file.isEmpty()) {
        return true;
    }
    if (file.getSize() > MAX_FILE_SIZE) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            "File size should be less than 2MB")
            .addConstraintViolation();
        return false;
    }
    return true;
}
```

**Fixed Code (Secure):**
```java
package com.scm.contactmanager.validators;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private static final long MAX_FILE_SIZE = 1024 * 1024 * 2; // 2MB
    
    // Magic numbers (file signatures) for validation
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G'};
    private static final byte[] GIF_MAGIC = {'G', 'I', 'F'};
    
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(Arrays.asList(
        "image/jpeg",
        "image/png",
        "image/gif"
    ));
    
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        "jpg",
        "jpeg",
        "png",
        "gif"
    ));

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;
        }

        // 1. Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            addViolation(context, "File size should be less than 2MB");
            return false;
        }

        // 2. Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            addViolation(context, "Only JPEG, PNG, and GIF images are allowed");
            return false;
        }

        // 3. Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !hasAllowedExtension(filename)) {
            addViolation(context, "Invalid file extension");
            return false;
        }

        // 4. Check magic number (file signature)
        try {
            byte[] fileBytes = file.getBytes();
            if (!isValidImageMagicNumber(fileBytes, contentType)) {
                addViolation(context, "File content does not match the claimed type");
                return false;
            }
        } catch (IOException e) {
            addViolation(context, "Unable to verify file content");
            return false;
        }

        return true;
    }

    private boolean hasAllowedExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return false;
        }
        String extension = filename.substring(lastDot + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private boolean isValidImageMagicNumber(byte[] bytes, String contentType) {
        if (bytes.length < 4) {
            return false;
        }

        // Check for JPEG
        if ("image/jpeg".equals(contentType)) {
            return bytes[0] == JPEG_MAGIC[0] && 
                   bytes[1] == JPEG_MAGIC[1] && 
                   bytes[2] == JPEG_MAGIC[2];
        }

        // Check for PNG
        if ("image/png".equals(contentType)) {
            return bytes[0] == PNG_MAGIC[0] && 
                   bytes[1] == PNG_MAGIC[1] && 
                   bytes[2] == PNG_MAGIC[2] && 
                   bytes[3] == PNG_MAGIC[3];
        }

        // Check for GIF
        if ("image/gif".equals(contentType)) {
            return bytes[0] == GIF_MAGIC[0] && 
                   bytes[1] == GIF_MAGIC[1] && 
                   bytes[2] == GIF_MAGIC[2];
        }

        return false;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
```

---

## FIX #5: Add Rate Limiting to API Endpoints 🟠 HIGH

**File 1:** Add dependency to `pom.xml`

```xml
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

**File 2:** Create `src/main/java/com/scm/contactmanager/security/RateLimitingFilter.java`

```java
package com.scm.contactmanager.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Bandwidth limit = Bandwidth.classic(
        100,  // 100 requests
        Refill.intervally(100, Duration.ofMinutes(1))  // per minute
    );
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

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

        String key = getClientKey(request);
        Bucket bucket = cache.computeIfAbsent(key, k -> 
            Bucket4j.builder()
                .addLimit(limit)
                .build()
        );

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Rate limit exceeded. Max 100 requests per minute.\"}"
            );
        }
    }

    private String getClientKey(HttpServletRequest request) {
        // Use IP address as key, or better: use authenticated user ID if available
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
```

---

## Summary of Fixes

| # | Severity | Fix | Effort | Time |
|---|----------|-----|--------|------|
| 1 | 🔴 CRITICAL | Remove DB password | 5 min | Now |
| 2 | 🔴 CRITICAL | Replace System.out | 30 min | Now |
| 3 | 🔴 CRITICAL | Validate sort params | 15 min | Now |
| 4 | 🟠 HIGH | File upload validation | 45 min | Today |
| 5 | 🟠 HIGH | Rate limiting | 30 min | Today |
| 6 | 🟠 HIGH | CSRF whitelist fix | 10 min | Today |
| 7 | 🟠 HIGH | Token security | 60 min | This week |
| 8 | 🟡 MEDIUM | Log sanitization | 45 min | This week |
| 9 | 🟡 MEDIUM | Account lockout | 90 min | This sprint |
| 10 | 🟡 MEDIUM | Parameter validation | 30 min | This sprint |

**Total Time to Fix Critical: ~50 minutes**  
**Total Time to Fix High: ~2.5 hours**

---

**Next:** Follow this guide to implement fixes in order of priority!
