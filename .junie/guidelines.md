# Spring WebFlux and R2DBC Best Practices

This document outlines the best practices for developing with Spring WebFlux and R2DBC in our project.

## Table of Contents
1. [Reactive Programming Principles](#reactive-programming-principles)
2. [Project Structure](#project-structure)
3. [Model Layer](#model-layer)
4. [Repository Layer](#repository-layer)
5. [Service Layer](#service-layer)
6. [Controller Layer](#controller-layer)
7. [Error Handling](#error-handling)
8. [Testing](#testing)
9. [Performance Considerations](#performance-considerations)
10. [Logging](#logging)

## Reactive Programming Principles

### Do's
- **Use reactive types consistently**: Always return `Mono` or `Flux` from methods that are part of a reactive chain.
- **Avoid blocking operations**: Never use blocking calls in a reactive pipeline.
- **Handle backpressure**: Be aware of backpressure and use operators like `limitRate()` or `sample()` when dealing with high-volume data streams.
- **Use reactive operators**: Leverage the rich set of operators provided by Project Reactor.
- **Subscribe explicitly**: Always subscribe to reactive streams or return them to a subscriber.

### Don'ts
- **Don't mix reactive and imperative code**: Avoid mixing blocking and non-blocking code.
- **Don't use `block()` in production code**: Using `block()` defeats the purpose of reactive programming.
- **Don't return `null`**: Return `Mono.empty()` instead of `null`.
- **Don't use `defaultIfEmpty(null)`**: Use `defaultIfEmpty(someValue)` with a non-null value or just let the Mono complete empty.

## Project Structure

Follow the standard Spring Boot project structure:

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── qiwitest/
│   │               ├── controller/
│   │               ├── dto/
│   │               ├── model/
│   │               ├── repository/
│   │               ├── service/
│   │               └── QiwiTestApplication.java
│   └── resources/
│       └── application.properties
└── test/
    ├── java/
    │   └── com/
    │       └── example/
    │           └── qiwitest/
    │               ├── controller/
    │               ├── repository/
    │               └── service/
    └── resources/
        └── application-test.properties
```

## Model Layer

### Entity Classes
- Use `@Table` annotation from Spring Data R2DBC.
- Use `@Id` annotation for primary keys.
- Avoid using JPA-specific annotations like `@Entity`, `@Column`, etc.
- Provide a default constructor for R2DBC to use.
- Keep entities simple and focused on representing database tables.

Example:
```java
@Table("CLIENTS")
public class Client {
    @Id
    private Long id;
    private String login;
    private String password;
    private BigDecimal balance = BigDecimal.ZERO;
    
    // Default constructor
    public Client() {
    }
    
    // Other constructors, getters, and setters
}
```

## Repository Layer

- Extend `ReactiveCrudRepository` for basic CRUD operations.
- Define custom query methods using method naming conventions.
- Use `@Query` annotation for complex queries.
- Return `Mono<T>` for single results and `Flux<T>` for multiple results.

Example:
```java
@Repository
public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {
    Mono<Client> findByLogin(String login);
    
    @Query("SELECT * FROM clients WHERE balance > :minBalance")
    Flux<Client> findClientsWithBalanceGreaterThan(BigDecimal minBalance);
}
```

## Service Layer

- Use constructor injection for dependencies.
- Return reactive types (`Mono<T>` or `Flux<T>`).
- Handle errors using reactive operators like `onErrorResume()`, `onErrorReturn()`, etc.
- Use `@Transactional` for methods that need transaction support.
- Log errors and important events.

Example:
```java
@Service
public class ClientService {
    private final ClientRepository clientRepository;
    
    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
    
    public Mono<Client> findByLogin(String login) {
        return clientRepository.findByLogin(login)
            .doOnError(e -> logger.error("Error finding client by login: {}", login, e));
    }
    
    @Transactional
    public Mono<Client> createClient(String login, String password) {
        return Mono.just(new Client(login, password))
            .flatMap(clientRepository::save)
            .doOnError(e -> logger.error("Error creating client with login: {}", login, e));
    }
}
```

## Controller Layer

- Use `@RestController` annotation.
- Return `Mono<ResponseEntity<T>>` or `Flux<T>` from controller methods.
- Use `WebFlux` annotations like `@GetMapping`, `@PostMapping`, etc.
- Handle validation and error cases.
- Use proper HTTP status codes.

Example:
```java
@RestController
public class ApiController {
    private final ClientService clientService;
    
    @Autowired
    public ApiController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    @PostMapping(value = "/", 
                consumes = MediaType.APPLICATION_XML_VALUE, 
                produces = MediaType.APPLICATION_XML_VALUE)
    public Mono<ResponseEntity<ResponseDto>> process(@RequestBody RequestDto request) {
        // Validate request
        if (request.getRequestType() == null) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        
        // Process request
        return clientService.findByLogin(request.getExtraValue("login"))
            .flatMap(client -> {
                // Process client
                return Mono.just(ResponseEntity.ok(new ResponseDto(0)));
            })
            .switchIfEmpty(Mono.just(ResponseEntity.ok(new ResponseDto(3))))
            .onErrorResume(e -> Mono.just(ResponseEntity.ok(new ResponseDto(2))));
    }
}
```

## Error Handling

- Use reactive error handling operators like `onErrorResume()`, `onErrorReturn()`, etc.
- Create a global error handler using `@ControllerAdvice` and `@ExceptionHandler`.
- Return appropriate HTTP status codes.
- Log errors with sufficient context.

Example:
```java
@ControllerAdvice
public class GlobalErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorHandler.class);
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleException(Exception ex) {
        logger.error("Unhandled exception", ex);
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Internal server error")));
    }
    
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(ValidationException ex) {
        logger.warn("Validation error", ex);
        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getMessage())));
    }
}
```

## Testing

- Use `@WebFluxTest` for controller tests.
- Use `@DataR2dbcTest` for repository tests.
- Use `StepVerifier` to test reactive streams.
- Use `WebTestClient` for end-to-end tests.
- Use `@MockBean` or `@MockitoBean` to mock dependencies.
- Test both success and error scenarios.

Example:
```java
@WebFluxTest(controllers = ApiController.class)
@Import(TestConfig.class)
public class ApiControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private ClientService clientService;
    
    @Test
    public void shouldReturnErrorForNoClient() {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(Mono.empty());
        
        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(getBalanceXml())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .xpath("/response/result-code").isEqualTo("3");
    }
}
```

## Performance Considerations

- **Connection Pooling**: Configure R2DBC connection pooling appropriately.
- **Caching**: Use reactive caching for frequently accessed data.
- **Pagination**: Use pagination for large result sets.
- **Timeouts**: Set appropriate timeouts for reactive operations.
- **Resource Cleanup**: Ensure proper cleanup of resources using `doOnTerminate()` or `doFinally()`.

Example configuration:
```properties
# R2DBC connection pooling
spring.r2dbc.pool.enabled=true
spring.r2dbc.pool.initial-size=5
spring.r2dbc.pool.max-size=10
spring.r2dbc.pool.max-idle-time=30m
```

## Logging

- Use SLF4J for logging.
- Configure appropriate log levels for different environments.
- Log important events and errors.
- Include relevant context in log messages.
- Avoid excessive logging in production.

Example:
```java
private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

public Mono<Client> findByLogin(String login) {
    logger.debug("Finding client by login: {}", login);
    return clientRepository.findByLogin(login)
        .doOnSuccess(client -> {
            if (client != null) {
                logger.debug("Found client with login: {}", login);
            } else {
                logger.debug("No client found with login: {}", login);
            }
        })
        .doOnError(e -> logger.error("Error finding client by login: {}", login, e));
}
```

---

By following these best practices, we can ensure that our Spring WebFlux and R2DBC application is maintainable, performant, and reliable.