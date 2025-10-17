3# Paragon - Codebase Standards & Architecture

This document describes the architectural patterns, coding standards, and development practices for the Paragon project.

## Architecture Overview

This project follows **Domain-Driven Design (DDD)**, **Clean Architecture**, and **CQRS (Command Query Responsibility Segregation)** principles. The codebase is organized into distinct layers with clear separation of concerns.

### Layer Structure

```
com.paragon/
├── api/                    # API/Presentation Layer
├── application/            # Application Layer (Use Cases)
├── domain/                 # Domain Layer (Business Logic)
└── infrastructure/         # Infrastructure Layer (Technical Details)
```

## Layer Responsibilities

### 1. API Layer (`com.paragon.api`)

**Purpose**: HTTP endpoints, DTOs, and request/response handling.

**Key Characteristics**:
- Controllers expose REST endpoints
- Uses `CompletableFuture` for asynchronous processing with `TaskExecutor` thread pools
- Servlet threads are immediately freed; work is delegated to worker threads
- DTOs are used for request/response serialization (no domain models exposed)
- Controllers are thin orchestrators that delegate to Command/Query Handlers

**Structure**:
- `*Controller.java` - REST controllers
- `dtos/` - Request and Response DTOs
- `errorhandling/` - Global error handling

**Example Pattern**:
```java
@PostMapping
public CompletableFuture<ResponseEntity<ResponseDto<RegisterStaffAccountResponseDto>>> register(@RequestBody RegisterStaffAccountRequestDto requestDto) {
    return CompletableFuture.supplyAsync(() -> {
        var command = createCommand(requestDto);
        var commandResponse = commandHandler.handle(command);
        var responseDto = new ResponseDto<>(createResponseDto(commandResponse), null);
        return ResponseEntity.ok(responseDto);
    }, taskExecutor);
}
```

### 2. Application Layer (`com.paragon.application`)

**Purpose**: Orchestrates use cases, coordinates between domain and infrastructure.

**Key Characteristics**:
- **Commands**: Write operations that change state
- **Queries**: Read operations that return data
- **Handlers**: Orchestrators that execute commands/queries
- Handlers do NOT contain business logic (that belongs in domain layer)
- Handlers coordinate between repositories, aggregates, and event bus
- Exception translation layer (Domain/Infra exceptions → App exceptions)

**Structure**:
```
application/
├── commands/
│   ├── CommandHandler.java (interface)
│   └── [usecase]/
│       ├── [UseCase]Command.java
│       ├── [UseCase]CommandHandler.java
│       └── [UseCase]CommandResponse.java
├── queries/
│   ├── QueryHandler.java (interface)
│   └── [usecase]/
│       ├── [UseCase]Query.java
│       ├── [UseCase]QueryHandler.java
│       └── [UseCase]QueryResponse.java
├── common/
│   └── exceptions/        # Application-level exceptions
├── context/              # Actor/request context
└── events/               # Event bus and handlers
```

**Handler Pattern**:
- Retrieve requesting actor from `ActorContext`
- Validate permissions and business rules via domain aggregates
- Delegate business logic to domain aggregates
- Persist changes via repositories
- Publish domain events via `EventBus`
- Catch and translate domain/infrastructure exceptions

### 3. Domain Layer (`com.paragon.domain`)

**Purpose**: Core business logic, rules, and domain models.

**Key Characteristics**:
- **Pure business logic** - no external dependencies
- **Aggregates**: Consistency boundaries, encapsulate business rules
- **Entities**: Objects with identity
- **Value Objects**: Immutable objects defined by their attributes
- **Domain Events**: Represent significant business occurrences
- **Repository Interfaces**: Define persistence contracts (implementations in infrastructure)
- All business invariants are enforced here

**Structure**:
```
domain/
├── models/
│   ├── aggregates/       # AggregateRoot, EventSourcedAggregate
│   ├── entities/         # Entity base class and entities
│   ├── valueobjects/     # ValueObject base class and VOs
│   └── constants/        # Domain constants (e.g., SystemPermissions)
├── interfaces/           # Repository interfaces
├── events/               # Domain events
├── enums/                # Domain enumerations
└── exceptions/           # Domain-specific exceptions
```

**Aggregate Pattern**:
- Factory methods for creation (e.g., `StaffAccount.register(...)`)
- Business logic encapsulated in methods
- Validation in private assertion methods
- Domain events enqueued via `EventSourcedAggregate`
- Version control for optimistic locking

**Value Object Pattern**:
- Immutable
- Factory methods: `of()`, `from()`, `generate()` (for IDs)
- Self-validating (throw domain exceptions on invalid input)
- Equality based on value, not identity

### 4. Infrastructure Layer (`com.paragon.infrastructure`)

**Purpose**: Technical implementations, external dependencies, I/O operations.

**Key Characteristics**:
- Implements domain repository interfaces
- Database access via JDBC
- External service integrations
- Security/authentication
- Configuration

**Structure**:
```
infrastructure/
├── persistence/
│   ├── repos/            # Repository implementations
│   ├── jdbc/             # JDBC helpers and utilities
│   ├── daos/             # Data Access Objects (DB ↔ Domain mapping)
│   ├── readmodels/       # Query-optimized read models
│   └── exceptions/       # Infrastructure exceptions
├── audit/                # Audit trail infrastructure
├── config/               # Spring configuration
├── externalservices/     # Third-party integrations
└── security/             # Authentication/authorization
```

**Repository Implementation Pattern**:
```java
@Repository
public class StaffAccountWriteRepoImpl implements StaffAccountWriteRepo {
    private final WriteJdbcHelper jdbcHelper;

    @Override
    public void create(StaffAccount staffAccount) {
        List<SqlStatement> sqlStatements = new ArrayList<>();

        // 1. Build SQL string (use text blocks for readability)
        String insertSql = """
            INSERT INTO staff_accounts
            (id, username, email, ...)
            VALUES
            (:id, :username, :email, ...)
        """;

        // 2. Build parameters using SqlParamsBuilder
        SqlParamsBuilder params = new SqlParamsBuilder()
            .add("id", staffAccount.getId().getValue())
            .add("username", staffAccount.getUsername().getValue())
            .add("email", staffAccount.getEmail() != null ? staffAccount.getEmail().getValue() : null);

        sqlStatements.add(new SqlStatement(insertSql, params));

        // 3. Execute via JDBC helper
        jdbcHelper.executeMultiple(sqlStatements);
    }
}
```

**DAO Pattern**:
- Java records that map directly to database tables
- Conversion methods to/from domain models (e.g., `toStaffAccount()`, `fromStaffAccount()`)
- Used for both reads and writes

## Testing Standards

We follow **Test-Driven Development (TDD)** and maintain extensive test coverage with both **Unit Tests** and **Integration Tests**.

### Test Organization

```
src/test/java/com/paragon/
├── domain/              # Domain unit tests
├── application/         # Application layer unit tests
├── infrastructure/      # Infrastructure unit tests
├── integration/         # Integration tests
│   ├── api/            # API integration tests
│   └── persistence/    # Persistence integration tests
└── helpers/            # Test utilities and fixtures
```

### Unit Testing Patterns

**Domain Layer Tests**:
- Test aggregate factory methods
- Test business rule enforcement
- Test domain event generation
- Use JUnit 5 `@Nested` classes to organize tests by method/scenario
- Use AssertJ for fluent assertions

**Application Layer Tests**:
- Mock repositories, event bus, actor context
- Use `ArgumentCaptor` to verify interactions
- Test exception handling and translation
- Verify correct delegation to domain and infrastructure

**Infrastructure Layer Tests**:
- Mock JDBC helpers
- Verify SQL construction and parameter binding
- Test DAO mappings
- Verify exception propagation

**Testing Libraries**:
- JUnit 5
- Mockito (mocking)
- AssertJ (assertions)

**Example Unit Test Structure**:
```java
public class StaffAccountTests {
    @Nested
    class Register {
        // Setup common test data

        @Test
        void givenValidInput_shouldCreateStaffAccount() {
            // Given

            // When

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        void givenInvalidInput_shouldThrowException() {
            // Given

            // When & Then
            assertThatThrownBy(() -> StaffAccount.register(...))
                .isInstanceOf(StaffAccountException.class);
        }
    }
}
```

### Integration Testing Patterns

**API Integration Tests**:
- Extend `IntegrationTestBase` (provides Spring context, MockMvc, etc.)
- Test full request → response flow
- Verify HTTP status codes and response bodies
- Use `TestJdbcHelper` for database setup/verification
- Test async handling with `asyncDispatch()`

**Persistence Integration Tests**:
- Test actual database interactions
- Verify data is correctly persisted and retrieved
- Test transaction boundaries

**Example Integration Test**:
```java
@Nested
class Register extends IntegrationTestBase {
    private final TestJdbcHelper jdbcHelper;

    @Test
    void shouldRegisterStaffAccount() throws Exception {
        // Given
        RegisterStaffAccountRequestDto requestDto = createValidRequest();

        // When
        MvcResult result = mockMvc.perform(
            post("/v1/staff-accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
        ).andReturn();
        result = mockMvc.perform(asyncDispatch(result)).andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
```

## Code Quality Standards

### General Principles

1. **Clean Code**: Self-documenting code with meaningful names
2. **SOLID Principles**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
3. **DRY**: Don't Repeat Yourself
4. **YAGNI**: You Aren't Gonna Need It
5. **Explicit over Implicit**: Clear, readable code over clever tricks

### Naming Conventions

- **Aggregates**: Nouns (e.g., `StaffAccount`)
- **Value Objects**: Descriptive nouns (e.g., `Username`, `Email`)
- **Commands**: Verb + Noun + "Command" (e.g., `RegisterStaffAccountCommand`)
- **Queries**: Verb + Noun + "Query" (e.g., `GetAllStaffAccountsQuery`)
- **Handlers**: UseCase + "CommandHandler" or "QueryHandler"
- **DTOs**: UseCase + "RequestDto" or "ResponseDto"
- **Tests**: Class + "Tests", method: `givenX_whenY_thenZ` or `shouldDoX_whenY`

### Exception Handling

Three-tier exception hierarchy:

1. **Domain Exceptions**: Business rule violations (e.g., `StaffAccountException`)
2. **Infrastructure Exceptions**: Technical failures (e.g., `InfraException`)
3. **Application Exceptions**: Translated exceptions for API layer (e.g., `AppException`)

Handlers catch domain/infra exceptions and translate to app exceptions with appropriate HTTP status codes.

### Logging

- Use SLF4J with `@Slf4j` annotation
- Log key business events (info level)
- Log exceptions with context (error level)
- Include relevant identifiers in log messages

## Development Workflow

### Incremental Changes

- Keep each change small and focused (single responsibility)
- Each logical change should be its own commit
- Commit to main after completing and testing each slice
- Write tests BEFORE implementation (TDD)

### Change Process

1. **Plan**: Break down feature into small, testable slices
2. **Test**: Write failing test(s) for the slice
3. **Implement**: Write minimal code to make tests pass
4. **Refactor**: Clean up code while keeping tests green
5. **Commit**: Commit the completed slice to main
6. **Repeat**: Move to next slice

## Event-Driven Architecture

- Aggregates emit **Domain Events** when significant business actions occur
- Events are enqueued in aggregates and dequeued by handlers
- `EventBus` publishes events to registered handlers
- Event handlers perform side effects (e.g., audit logging, notifications)
- Events enable loose coupling between components

## Summary

This codebase emphasizes:
- **Clear separation of concerns** across layers
- **Rich domain models** with encapsulated business logic
- **Asynchronous API** with thread pool execution
- **Comprehensive testing** with TDD approach
- **Event-driven design** for side effects
- **Type safety** with value objects
- **Explicit error handling** with domain exceptions
- **Clean, maintainable code** following industry best practices

When implementing new features:
1. Start with domain models and tests
2. Add application layer orchestration
3. Implement infrastructure concerns
4. Wire up API endpoints
5. Write integration tests
6. Commit incrementally

Always maintain these established patterns and standards.
