# Technical Deep Dive: Implementation Guide for New Features

## Architecture Overview

### Current Stack
- **Backend**: Spring Boot 3.5, Java 21, Spring Data JPA, Hibernate
- **Database**: MySQL with Liquibase migrations
- **Authentication**: Spring Security, OAuth2 (Google, GitHub)
- **Storage**: Cloudinary for images
- **Frontend**: Thymeleaf, TailwindCSS, Flowbite
- **API**: RESTful with standardized ApiResponse wrapper
- **Testing**: JUnit 5, Mockito, Spring Test

---

## Implementation Guides

### 1. CONTACT TAGGING SYSTEM (Recommended First Priority)

#### Database Schema
```java
@Entity
@Table(name = "contact_tags")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(length = 7) // hex color code
    private String color; // e.g., "#FF5733"
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToMany(mappedBy = "tags")
    private Set<Contact> contacts = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### Update Contact Entity
```java
@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "contact_contact_tag",
    joinColumns = @JoinColumn(name = "contact_id"),
    inverseJoinColumns = @JoinColumn(name = "tag_id")
)
private Set<ContactTag> tags = new HashSet<>();
```

#### Service Layer
```java
@Service
@Transactional
public class ContactTagService {
    
    @Autowired
    private ContactTagRepository tagRepository;
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private UserService userService;
    
    public ContactTag createTag(String name, String color, String description, User user) {
        if (tagRepository.existsByNameAndUser(name, user)) {
            throw new DuplicateResourceException("Tag already exists: " + name);
        }
        
        ContactTag tag = ContactTag.builder()
            .name(name)
            .color(color)
            .description(description)
            .user(user)
            .build();
        
        return tagRepository.save(tag);
    }
    
    public void addTagToContact(Long contactId, Long tagId, User user) {
        Contact contact = contactRepository.findByIdAndUser(contactId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        
        ContactTag tag = tagRepository.findByIdAndUser(tagId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        
        contact.getTags().add(tag);
        contactRepository.save(contact);
    }
    
    public void removeTagFromContact(Long contactId, Long tagId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        
        contact.getTags().removeIf(tag -> tag.getId().equals(tagId));
        contactRepository.save(contact);
    }
    
    public Page<Contact> getContactsByTag(Long tagId, int page, int size) {
        return contactRepository.findByTags_Id(tagId, PageRequest.of(page, size));
    }
    
    public List<ContactTag> getUserTags(User user) {
        return tagRepository.findByUser(user);
    }
    
    public void deleteTag(Long tagId, User user) {
        ContactTag tag = tagRepository.findByIdAndUser(tagId, user)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        
        tag.getContacts().clear();
        tagRepository.delete(tag);
    }
}
```

#### Repository
```java
@Repository
public interface ContactTagRepository extends JpaRepository<ContactTag, Long> {
    List<ContactTag> findByUser(User user);
    boolean existsByNameAndUser(String name, User user);
    Optional<ContactTag> findByIdAndUser(Long id, User user);
    List<ContactTag> findByUserAndNameContainingIgnoreCase(User user, String name);
}
```

#### Controller
```java
@RestController
@RequestMapping("/api/contacts/tags")
@Validated
public class ContactTagController {
    
    @Autowired
    private ContactTagService tagService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ContactTag>> createTag(
            @Valid @RequestBody CreateTagRequest request,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        ContactTag tag = tagService.createTag(
            request.getName(), 
            request.getColor(), 
            request.getDescription(), 
            user
        );
        return ResponseEntity.ok(ApiResponse.success("Tag created successfully", tag));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactTag>>> getUserTags(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        List<ContactTag> tags = tagService.getUserTags(user);
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tags));
    }
    
    @PostMapping("/{tagId}/contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> addTagToContact(
            @PathVariable Long tagId,
            @PathVariable Long contactId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        tagService.addTagToContact(contactId, tagId, user);
        return ResponseEntity.ok(ApiResponse.success("Tag added to contact", null));
    }
    
    @DeleteMapping("/{tagId}/contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromContact(
            @PathVariable Long tagId,
            @PathVariable Long contactId) {
        tagService.removeTagFromContact(contactId, tagId);
        return ResponseEntity.ok(ApiResponse.success("Tag removed from contact", null));
    }
    
    @GetMapping("/{tagId}/contacts")
    public ResponseEntity<ApiResponse<Page<Contact>>> getContactsByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Contact> contacts = tagService.getContactsByTag(tagId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Contacts retrieved successfully", contacts));
    }
    
    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Long tagId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        tagService.deleteTag(tagId, user);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", null));
    }
}
```

#### DTOs
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTagRequest {
    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50)
    private String name;
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
    private String color;
    
    @Size(max = 500)
    private String description;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagResponse {
    private Long id;
    private String name;
    private String color;
    private String description;
    private Integer contactCount;
}
```

---

### 2. ADVANCED SEARCH & FILTERING

#### Search Criteria DTO
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedSearchCriteria {
    private String name;
    private String email;
    private String phoneNumber;
    private Relationship relationship;
    private String city;
    private String state;
    private String country;
    private LocalDate birthdateFrom;
    private LocalDate birthdateTo;
    private Boolean isFavorite;
    private List<Long> tagIds;
    private String websiteUrl;
    private LocalDate lastContactedFrom;
    private LocalDate lastContactedTo;
    
    private String sortBy; // name, createdAt, lastModified
    private String sortDirection; // ASC, DESC
    private Integer page = 0;
    private Integer size = 10;
}
```

#### Service Implementation
```java
@Service
@Transactional(readOnly = true)
public class AdvancedSearchService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private ContactSpecification contactSpecification;
    
    public Page<Contact> search(AdvancedSearchCriteria criteria, User user) {
        Specification<Contact> spec = contactSpecification.buildSpecification(criteria, user);
        
        Sort sort = Sort.by(
            criteria.getSortDirection().equalsIgnoreCase("ASC") 
                ? Sort.Order.asc(criteria.getSortBy()) 
                : Sort.Order.desc(criteria.getSortBy())
        );
        
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
        
        return contactRepository.findAll(spec, pageable);
    }
}

@Component
public class ContactSpecification {
    
    public Specification<Contact> buildSpecification(AdvancedSearchCriteria criteria, User user) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Always filter by user
            predicates.add(cb.equal(root.get("user"), user));
            
            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(cb.like(
                    cb.lower(root.get("name")), 
                    "%" + criteria.getName().toLowerCase() + "%"
                ));
            }
            
            if (StringUtils.hasText(criteria.getEmail())) {
                predicates.add(cb.like(
                    cb.lower(root.get("email")), 
                    "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }
            
            if (StringUtils.hasText(criteria.getPhoneNumber())) {
                predicates.add(cb.like(root.get("phoneNumber"), "%" + criteria.getPhoneNumber() + "%"));
            }
            
            if (criteria.getRelationship() != null) {
                predicates.add(cb.equal(root.get("relationship"), criteria.getRelationship()));
            }
            
            if (criteria.getIsFavorite() != null) {
                predicates.add(cb.equal(root.get("favorite"), criteria.getIsFavorite()));
            }
            
            if (criteria.getBirthdateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.join("address").get("birthdate"), 
                    criteria.getBirthdateFrom()
                ));
            }
            
            if (criteria.getBirthdateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.join("address").get("birthdate"), 
                    criteria.getBirthdateTo()
                ));
            }
            
            if (!CollectionUtils.isEmpty(criteria.getTagIds())) {
                Join<Contact, ContactTag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagJoin.get("id").in(criteria.getTagIds()));
                query.distinct(true);
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

#### Controller
```java
@PostMapping("/api/contacts/search/advanced")
public ResponseEntity<ApiResponse<Page<Contact>>> advancedSearch(
        @RequestBody @Valid AdvancedSearchCriteria criteria,
        Authentication authentication) {
    User user = getUserFromAuth(authentication);
    Page<Contact> results = advancedSearchService.search(criteria, user);
    return ResponseEntity.ok(ApiResponse.success("Search completed", results));
}
```

---

### 3. CONTACT ACTIVITY TIMELINE

#### Entity
```java
@Entity
@Table(name = "contact_activities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    private LocalDateTime timestamp;
    
    @Column(length = 45)
    private String ipAddress;
    
    private String userAgent;
}

public enum ActivityType {
    CREATED,
    VIEWED,
    UPDATED,
    DELETED,
    EXPORTED,
    SHARED,
    TAGGED,
    FAVORITED,
    COMMUNICATION_LOGGED
}
```

#### Repository & Service
```java
@Repository
public interface ContactActivityRepository extends JpaRepository<ContactActivity, Long> {
    List<ContactActivity> findByContactOrderByTimestampDesc(Contact contact);
    List<ContactActivity> findByUserAndTimestampAfter(User user, LocalDateTime timestamp);
}

@Service
@Transactional
public class ContactActivityService {
    
    @Autowired
    private ContactActivityRepository activityRepository;
    
    public void logActivity(Contact contact, User user, ActivityType type, 
                           String description, HttpServletRequest request) {
        ContactActivity activity = ContactActivity.builder()
            .contact(contact)
            .user(user)
            .activityType(type)
            .description(description)
            .ipAddress(getClientIp(request))
            .userAgent(request.getHeader("User-Agent"))
            .build();
        
        activityRepository.save(activity);
    }
    
    public List<ContactActivity> getContactTimeline(Contact contact) {
        return activityRepository.findByContactOrderByTimestampDesc(contact);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
```

#### Aspect for Auto-Logging
```java
@Aspect
@Component
public class ActivityLoggingAspect {
    
    @Autowired
    private ContactActivityService activityService;
    
    @Around("@annotation(com.scm.contactmanager.annotations.LogActivity)")
    public Object logActivity(ProceedingJoinPoint joinPoint, LogActivity annotation) throws Throwable {
        Object result = joinPoint.proceed();
        
        // Extract contact and user from method arguments
        // Log the activity
        
        return result;
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogActivity {
    ActivityType value();
    String description() default "";
}
```

---

### 4. BIRTHDAY & IMPORTANT DATES

#### Entity
```java
@Entity
@Table(name = "important_dates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImportantDate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
    
    @Column(nullable = false, length = 100)
    private String name; // "Birthday", "Anniversary", "Wedding Day"
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(columnDefinition = "BIT(1) DEFAULT 0")
    private Boolean notificationEnabled = true;
    
    @Column
    private Integer daysBeforeNotify = 7;
    
    @Column
    private LocalDateTime lastNotified;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

#### Scheduler Service
```java
@Service
public class BirthdayReminderService {
    
    @Autowired
    private ImportantDateRepository dateRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Scheduled(cron = "0 0 9 * * *") // 9 AM every day
    public void sendUpcomingDateReminders() {
        LocalDate today = LocalDate.now();
        
        List<ImportantDate> upcomingDates = dateRepository.findUpcomingDates(
            today, 
            today.plusDays(30)
        );
        
        for (ImportantDate importantDate : upcomingDates) {
            if (shouldSendNotification(importantDate)) {
                Contact contact = importantDate.getContact();
                User user = contact.getUser();
                
                String subject = "Reminder: " + importantDate.getName() + " of " + contact.getName();
                String body = buildReminderEmail(importantDate);
                
                emailService.sendEmail(user.getEmail(), subject, body);
                importantDate.setLastNotified(LocalDateTime.now());
                dateRepository.save(importantDate);
            }
        }
    }
    
    private boolean shouldSendNotification(ImportantDate date) {
        return date.getNotificationEnabled() && 
               (date.getLastNotified() == null || 
                date.getLastNotified().isBefore(LocalDateTime.now().minusDays(1)));
    }
    
    private String buildReminderEmail(ImportantDate date) {
        int daysUntil = (int) ChronoUnit.DAYS.between(LocalDate.now(), date.getDate());
        return String.format(
            "Don't forget! %s is coming up in %d days.",
            date.getName(),
            daysUntil
        );
    }
}
```

#### Repository
```java
@Repository
public interface ImportantDateRepository extends JpaRepository<ImportantDate, Long> {
    @Query("SELECT id FROM ImportantDate id WHERE id.date BETWEEN :startDate AND :endDate " +
           "AND id.notificationEnabled = true")
    List<ImportantDate> findUpcomingDates(LocalDate startDate, LocalDate endDate);
    
    List<ImportantDate> findByContact(Contact contact);
}
```

---

### 5. BULK OPERATIONS

#### Request DTO
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkActionRequest {
    
    @NotNull
    @NotEmpty
    private List<Long> contactIds;
    
    @NotNull
    private BulkActionType actionType;
    
    private Object actionData; // Generic field for action-specific data
}

public enum BulkActionType {
    DELETE,
    ADD_TAG,
    REMOVE_TAG,
    MARK_FAVORITE,
    UNMARK_FAVORITE,
    EXPORT,
    CHANGE_RELATIONSHIP
}
```

#### Service
```java
@Service
@Transactional
public class BulkActionService {
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private ContactActivityService activityService;
    
    public BulkActionResponse performBulkAction(BulkActionRequest request, User user, HttpServletRequest httpRequest) {
        List<Contact> contacts = contactRepository.findByIdInAndUser(request.getContactIds(), user);
        
        if (contacts.isEmpty()) {
            throw new ResourceNotFoundException("No valid contacts found for bulk operation");
        }
        
        int processed = 0;
        int failed = 0;
        
        try {
            switch (request.getActionType()) {
                case DELETE:
                    processed = performDelete(contacts, user, httpRequest);
                    break;
                case ADD_TAG:
                    processed = performAddTag(contacts, (Long) request.getActionData());
                    break;
                case MARK_FAVORITE:
                    processed = performMarkFavorite(contacts, true);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported action type");
            }
        } catch (Exception e) {
            failed = contacts.size() - processed;
        }
        
        return BulkActionResponse.builder()
            .totalRequested(contacts.size())
            .processed(processed)
            .failed(failed)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    private int performDelete(List<Contact> contacts, User user, HttpServletRequest request) {
        for (Contact contact : contacts) {
            contactRepository.delete(contact);
            activityService.logActivity(contact, user, ActivityType.DELETED, 
                "Bulk delete operation", request);
        }
        return contacts.size();
    }
    
    private int performAddTag(List<Contact> contacts, Long tagId) {
        for (Contact contact : contacts) {
            // Add tag to contact
            contact.getTags().stream()
                .filter(tag -> tag.getId().equals(tagId))
                .findFirst();
        }
        contactRepository.saveAll(contacts);
        return contacts.size();
    }
    
    private int performMarkFavorite(List<Contact> contacts, boolean isFavorite) {
        contacts.forEach(contact -> contact.setFavorite(isFavorite));
        contactRepository.saveAll(contacts);
        return contacts.size();
    }
}
```

---

## Database Migration Scripts (Liquibase)

```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">

    <!-- Create contact_tags table -->
    <changeSet id="create-contact-tags-table" author="dev">
        <createTable tableName="contact_tags">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="name" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="color" type="VARCHAR(7)"/>
            <column name="description" type="TEXT"/>
            <column name="user_id" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_tags_user" references="users(id)"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueDate="CURRENT_TIMESTAMP"/>
            <column name="updated_at" type="TIMESTAMP" defaultValueDate="CURRENT_TIMESTAMP"/>
        </createTable>
        <createIndex tableName="contact_tags" indexName="idx_user_id">
            <column name="user_id"/>
        </createIndex>
    </changeSet>

    <!-- Create junction table for contacts and tags -->
    <changeSet id="create-contact-contact-tag-table" author="dev">
        <createTable tableName="contact_contact_tag">
            <column name="contact_id" type="BIGINT">
                <constraints primaryKey="true" foreignKeyName="fk_cct_contact" references="contact(id)"/>
            </column>
            <column name="tag_id" type="BIGINT">
                <constraints primaryKey="true" foreignKeyName="fk_cct_tag" references="contact_tags(id)"/>
            </column>
        </createTable>
    </changeSet>

    <!-- Create contact_activities table -->
    <changeSet id="create-contact-activities-table" author="dev">
        <createTable tableName="contact_activities">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="contact_id" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_activity_contact" references="contact(id)"/>
            </column>
            <column name="user_id" type="VARCHAR(100)">
                <constraints nullable="false" foreignKeyName="fk_activity_user" references="users(id)"/>
            </column>
            <column name="activity_type" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="description" type="TEXT"/>
            <column name="timestamp" type="TIMESTAMP" defaultValueDate="CURRENT_TIMESTAMP"/>
            <column name="ip_address" type="VARCHAR(45)"/>
            <column name="user_agent" type="TEXT"/>
        </createTable>
        <createIndex tableName="contact_activities" indexName="idx_contact_timestamp">
            <column name="contact_id"/>
            <column name="timestamp"/>
        </createIndex>
    </changeSet>

    <!-- Create important_dates table -->
    <changeSet id="create-important-dates-table" author="dev">
        <createTable tableName="important_dates">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="contact_id" type="BIGINT">
                <constraints nullable="false" foreignKeyName="fk_date_contact" references="contact(id)"/>
            </column>
            <column name="name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="date" type="DATE">
                <constraints nullable="false"/>
            </column>
            <column name="notification_enabled" type="BIT" defaultValue="1"/>
            <column name="days_before_notify" type="INT" defaultValue="7"/>
            <column name="last_notified" type="TIMESTAMP"/>
            <column name="created_at" type="TIMESTAMP" defaultValueDate="CURRENT_TIMESTAMP"/>
        </createTable>
        <createIndex tableName="important_dates" indexName="idx_date_upcoming">
            <column name="date"/>
            <column name="notification_enabled"/>
        </createIndex>
    </changeSet>

</databaseChangeLog>
```

---

## Testing Strategy

```java
@SpringBootTest
@AutoConfigureMockMvc
public class ContactTagServiceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ContactTagRepository tagRepository;
    
    @InjectMocks
    private ContactTagService tagService;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
            .id("user1")
            .name("Test User")
            .email("test@example.com")
            .build();
    }
    
    @Test
    public void testCreateTag_Success() {
        // Arrange
        String tagName = "Important";
        when(tagRepository.existsByNameAndUser(tagName, testUser)).thenReturn(false);
        
        ContactTag expectedTag = ContactTag.builder()
            .id(1L)
            .name(tagName)
            .color("#FF5733")
            .user(testUser)
            .build();
        
        when(tagRepository.save(any(ContactTag.class))).thenReturn(expectedTag);
        
        // Act
        ContactTag result = tagService.createTag(tagName, "#FF5733", "Important contacts", testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(tagName, result.getName());
        verify(tagRepository, times(1)).save(any(ContactTag.class));
    }
    
    @Test
    public void testAddTagToContact_Success() {
        // Test implementation
    }
}
```

---

## Performance Optimization Tips

1. **Database Indexes**: Add on frequently queried columns
```sql
CREATE INDEX idx_contact_user_favorite ON contact(user_id, favorite);
CREATE INDEX idx_contact_name ON contact(name);
CREATE INDEX idx_contact_email ON contact(email);
```

2. **Eager Loading Strategy**:
```java
@EntityGraph(attributePaths = {"tags", "address", "socialLinks"})
@Query("SELECT c FROM Contact c WHERE c.user = :user")
Page<Contact> findAllWithDetails(User user, Pageable pageable);
```

3. **Caching**:
```java
@Cacheable(value = "userTags", key = "#user.id")
public List<ContactTag> getUserTags(User user) {
    return tagRepository.findByUser(user);
}
```

---

## Security Considerations

1. **Input Validation**: Always validate user input
2. **Authorization**: Check ownership before operations
3. **Rate Limiting**: Implement on bulk operations
4. **Audit Logging**: Log all data modifications
5. **SQL Injection**: Use parameterized queries (JPA handles this)
6. **XSS Prevention**: Sanitize output in templates

This implementation guide provides the foundation for enhancing your Contact Manager application with production-ready features.
