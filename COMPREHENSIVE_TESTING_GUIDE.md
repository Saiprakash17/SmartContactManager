# Comprehensive Testing Guide - Contact Manager

## Overview

This document provides a complete testing strategy and implementation guide for achieving **100% code coverage** in the Contact Manager application. The testing framework follows industry best practices using JUnit 5, Mockito, and Spring Boot Test utilities.

---

## Testing Architecture

### Testing Pyramid

```
                    ┌─────────────────┐
                    │  Integration    │ ~100 tests
                    │     Tests       │ (5%)
                    ├─────────────────┤
                    │  Controller     │ ~250 tests
                    │  & Service      │ (25%)
                    ├─────────────────┤
                    │   Unit Tests    │ ~650 tests
                    │  (Entities,     │ (70%)
                    │   DTOs, Utils)  │
                    └─────────────────┘
```

### Testing Layers

| Layer | Classes | Test Type | Tools | Priority |
|-------|---------|-----------|-------|----------|
| **Entities** | 11 | Unit | JUnit 5, Assertions | ✅ Done |
| **Forms** | 7 | Unit | JUnit 5, Assertions | ✅ Done |
| **DTOs** | 5 | Unit | JUnit 5, Assertions | ✅ Done |
| **Services** | 15 | Integration | @SpringBootTest, Mockito | 🔄 In Progress |
| **Controllers** | 15 | Integration | @WebMvcTest, MockMvc | 🔄 In Progress |
| **Repositories** | 8 | Integration | @DataJpaTest | ⏳ Pending |
| **Security** | 5 | Integration | @SpringBootTest | ⏳ Pending |
| **Config** | 8 | Integration | @SpringBootTest | ⏳ Pending |

---

## Testing Patterns by Component Type

### 1. Entity Testing Pattern

**Location:** `src/test/java/com/scm/contactmanager/entities/`

**Key Annotations:**
- `@DisplayName` - Clear test descriptions
- `@Nested` - Organize related tests
- `@BeforeEach` - Setup fixtures

**Test Structure:**
```java
@DisplayName("ClassName Tests")
class ClassNameTest {
    
    private ClassName object;
    
    @BeforeEach
    void setUp() {
        // Initialize test data
        object = new ClassName("value1", "value2");
    }
    
    @Nested
    @DisplayName("Getter & Setter Tests")
    class GetterSetterTests {
        
        @Test
        @DisplayName("Should get and set property")
        void testPropertyGetterSetter() {
            object.setProperty("newValue");
            assertEquals("newValue", object.getProperty());
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        // Test business logic
    }
}
```

**What to Test:**
- ✅ All getters/setters
- ✅ Constructor variants (NoArgs, AllArgs, Builder)
- ✅ Default values
- ✅ Null/empty handling
- ✅ Enum support
- ✅ Relationships to other entities
- ✅ Complex business logic

**Test Count:** 40-60 per entity class

---

### 2. Form Testing Pattern

**Location:** `src/test/java/com/scm/contactmanager/forms/`

**Key Focus Areas:**
- Field validation
- Constraint enforcement (@NotBlank, @Email, @Pattern, etc.)
- Form lifecycle

**Test Structure:**
```java
@DisplayName("FormName Tests")
class FormNameTest {
    
    private FormName form;
    
    @BeforeEach
    void setUp() {
        form = FormName.builder()
                .field1("value1")
                .field2("value2")
                .build();
    }
    
    @Nested
    @DisplayName("Field1 Validation Tests")
    class Field1ValidationTests {
        
        @Test
        @DisplayName("Should accept valid field1")
        void testValidField1() {
            form.setField1("validValue");
            assertEquals("validValue", form.getField1());
        }
        
        @Test
        @DisplayName("Should accept field1 with special characters")
        void testField1WithSpecialChars() {
            form.setField1("Value's-Special");
            assertNotNull(form.getField1());
        }
    }
    
    @Nested
    @DisplayName("Complete Form Tests")
    class CompleteFormTests {
        
        @Test
        @DisplayName("Should create complete form with all fields")
        void testCompleteForm() {
            // Verify all fields set correctly
        }
    }
}
```

**What to Test:**
- ✅ Field constraints (@NotBlank, @Size, @Email, @Pattern)
- ✅ Valid value formats
- ✅ Invalid value handling
- ✅ Edge cases (min/max length, special characters)
- ✅ Null/empty scenarios
- ✅ Complete form scenario

**Test Count:** 50-100 per form class

---

### 3. Service Testing Pattern

**Location:** `src/test/java/com/scm/contactmanager/services/impl/`

**Key Annotations:**
- `@ExtendWith(MockitoExtension.class)` - Enable Mockito
- `@Mock` - Mock dependencies
- `@InjectMocks` - Inject mocked dependencies

**Test Structure:**
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceName Tests")
class ServiceNameTest {
    
    @Mock
    private DependencyRepository repository;
    
    @InjectMocks
    private ServiceNameImpl service;
    
    private TestEntity testEntity;
    
    @BeforeEach
    void setUp() {
        testEntity = TestEntity.builder().id(1L).build();
    }
    
    @Nested
    @DisplayName("Create Method Tests")
    class CreateMethodTests {
        
        @Test
        @DisplayName("Should create entity successfully")
        void testCreateSuccess() {
            when(repository.save(testEntity)).thenReturn(testEntity);
            
            TestEntity result = service.save(testEntity);
            
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(repository, times(1)).save(testEntity);
        }
    }
}
```

**What to Test:**
- ✅ CRUD operations (Create, Read, Update, Delete)
- ✅ Business logic
- ✅ Search/filter operations
- ✅ Validation
- ✅ Exception handling
- ✅ Mock repository interactions
- ✅ Transaction behavior

**Test Count:** 30-50 per service class
**Pattern:** Arrange → Act → Assert + Verify

---

### 4. Controller Testing Pattern

**Location:** `src/test/java/com/scm/contactmanager/controllers/`

**Key Annotations:**
- `@WebMvcTest` - Test only the controller layer
- `@Autowired MockMvc` - HTTP request builder
- `@MockBean` - Mock service dependencies

**Test Structure:**
```java
@WebMvcTest(ControllerName.class)
@DisplayName("ControllerName Tests")
class ControllerNameTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ServiceName service;
    
    private TestEntity testEntity;
    
    @BeforeEach
    void setUp() {
        testEntity = TestEntity.builder().id(1L).build();
    }
    
    @Nested
    @DisplayName("GET Endpoint Tests")
    class GetEndpointTests {
        
        @Test
        @DisplayName("Should return entity successfully")
        void testGetSuccess() throws Exception {
            when(service.getById(1L)).thenReturn(testEntity);
            
            mockMvc.perform(get("/api/entity/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
            
            verify(service, times(1)).getById(1L);
        }
        
        @Test
        @DisplayName("Should return 404 when entity not found")
        void testGetNotFound() throws Exception {
            when(service.getById(999L))
                    .thenThrow(new RuntimeException("Not found"));
            
            mockMvc.perform(get("/api/entity/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("POST Endpoint Tests")
    class PostEndpointTests {
        
        @Test
        @DisplayName("Should create entity successfully")
        void testCreateSuccess() throws Exception {
            when(service.save(any(TestEntity.class))).thenReturn(testEntity);
            
            mockMvc.perform(post("/api/entity")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEntity)))
                    .andExpect(status().isCreated());
        }
        
        @Test
        @DisplayName("Should return 400 for invalid data")
        void testCreateInvalidData() throws Exception {
            mockMvc.perform(post("/api/entity")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
```

**What to Test:**
- ✅ GET endpoints (retrieve data)
- ✅ POST endpoints (create data) - valid & invalid inputs
- ✅ PUT endpoints (update data) - valid & invalid inputs
- ✅ DELETE endpoints (remove data)
- ✅ Request validation (400 Bad Request)
- ✅ Not found scenarios (404)
- ✅ Authorization (401, 403)
- ✅ Content type handling
- ✅ Status codes (200, 201, 204, 400, 404, 500)

**Test Count:** 40-60 per controller
**HTTP Status Codes to Test:**
- 200 OK, 201 Created, 204 No Content
- 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
- 500 Internal Server Error

---

### 5. Repository Testing Pattern

**Location:** `src/test/java/com/scm/contactmanager/repositories/`

**Key Annotations:**
- `@DataJpaTest` - Test only repository layer
- `@ExtendWith(MockitoExtension.class)` or real database

**Test Structure:**
```java
@DataJpaTest
@DisplayName("RepositoryName Tests")
class RepositoryNameTest {
    
    @Autowired
    private RepositoryName repository;
    
    @Test
    @DisplayName("Should find entity by ID")
    void testFindById() {
        Optional<Entity> result = repository.findById(1L);
        assertTrue(result.isPresent());
    }
    
    @Test
    @DisplayName("Should save entity")
    void testSave() {
        Entity entity = Entity.builder().name("Test").build();
        Entity saved = repository.save(entity);
        assertNotNull(saved.getId());
    }
}
```

---

## Test Data Setup Strategies

### 1. Test Fixtures (Recommended for this project)

```java
@BeforeEach
void setUp() {
    testUser = User.builder()
            .id("user123")
            .name("John Doe")
            .email("john@example.com")
            .enabled(true)
            .build();
}
```

### 2. Data Builder Pattern

```java
User user = User.builder()
        .id("user123")
        .name("John Doe")
        .email("john@example.com")
        .build();
```

### 3. Test Data Factories

```java
private Contact createTestContact(String name, String email) {
    return Contact.builder()
            .id(1L)
            .name(name)
            .email(email)
            .build();
}
```

---

## Mockito Usage Guide

### Common Mockito Patterns

```java
// Setup mock return value
when(repository.findById(1L)).thenReturn(Optional.of(entity));

// Verify method was called
verify(repository, times(1)).save(entity);

//  Throw exception
when(repository.save(null)).thenThrow(new RuntimeException("Null error"));

// Use any() matcher
when(repository.save(any(Entity.class))).thenReturn(entity);

// Multiple calls with different returns
when(repository.findById(1L)).thenReturn(Optional.of(entity1));
when(repository.findById(2L)).thenReturn(Optional.of(entity2));

// Verify no interaction
verify(repository, never()).delete(any());

// Capture arguments
ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
verify(repository).save(captor.capture());
Contact captured = captor.getValue();
```

---

## Assertion Patterns

### Standard Assertions

```java
// Equality
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);

// Boolean
assertTrue(condition);
assertFalse(condition);

// Null checks
assertNull(actual);
assertNotNull(actual);

// Collections
assertEquals(3, list.size());
assertTrue(list.contains(element));

// Exceptions
assertThrows(ExceptionType.class, () -> method());

// No exception thrown
assertDoesNotThrow(() -> method());
```

---

## Test Coverage Goals

### By Component

| Component | Line Coverage | Branch Coverage | Method Coverage |
|-----------|---------------|-----------------|-----------------|
| Entities | 90%+ | 85%+ | 100% |
| Services | 95%+ | 90%+ | 100% |
| Controllers | 90%+ | 85%+ | 95%+ |
| Helpers | 95%+ | 90%+ | 100% |
| **Overall** | **92%+** | **87%+** | **98%+** |

### Measurement

```bash
# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

---

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=UserTest
mvn test -Dtest=ContactServiceImplTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=UserTest#testNameGetterSetter
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
```

### Run Tests in Parallel

```bash
mvn -T 1C test  # Run test classes in parallel
```

---

## Best Practices

### Do's ✅

- ✅ Use @DisplayName for clear descriptions
- ✅ One assertion focus per test method
- ✅ Use @Nested for logical organization
- ✅ Test both happy path and error scenarios
- ✅ Mock external dependencies
- ✅ Verify mock interactions
- ✅ Use meaningful variable names
- ✅ Keep tests independent
- ✅ Use equivalent test data
- ✅ Test edge cases and boundaries

### Don'ts ❌

- ❌ Don't use generic test names ("testMethod")
- ❌ Don't test multiple scenarios in one test
- ❌ Don't create unnecessary test dependencies
- ❌ Don't test framework code (Spring, JPA)
- ❌ Don't forget @BeforeEach setup
- ❌ Don't ignore test failures
- ❌ Don't create tight coupling with implementation
- ❌ Don't test private methods
- ❌ Don't hardcode test data
- ❌ Don't forget to assert

---

## Checklist for New Tests

When creating a new test class, ensure:

- [ ] Class named `ClassNameTest`
- [ ] `@DisplayName` annotation on class
- [ ] `@BeforeEach` setup method
- [ ] `@Nested` classes for logical grouping
- [ ] `@Test` methods with `@DisplayName`
- [ ] Proper assertions
- [ ] Mock verification where needed
- [ ] Tests for happy path
- [ ] Tests for error scenarios
- [ ] Tests for edge cases/boundaries
- [ ] No dependency between tests
- [ ] Clear and descriptive test names

---

## Coverage Report Example

```
Package              Classes  Methods  Lines
────────────────────────────────────────
entities/               11      98      340
services/impl/          15     215      680
controllers/            15     180      520
forms/                   7      68      210
────────────────────────────────────────
Total                   48     561     1750

Overall Coverage: 92% Line, 87% Branch, 98% Method
```

---

## Extending Tests

To extend tests to remaining classes, follow the same patterns:

1. **Create test file** in same package structure
2. **Add @DisplayName** with class+method details
3. **Setup @BeforeEach** with test data
4. **Use @Nested** for method/feature grouping
5. **Write 3-4 tests** per public method
6. **Test positive, negative, edge cases**
7. **Mock external dependencies**
8. **Verify critical interactions**

---

**Document Version:** 1.0
**Last Updated:** March 23, 2026
**Estimated Total Test Count:** 1000+ tests for 100% coverage
**Current Completion:** 25% (Phase 1-3 foundation completed)

