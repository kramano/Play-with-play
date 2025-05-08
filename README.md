# Qiwi Test Application

This application has been migrated from Play Framework to Spring Boot.

## Migration Details

### Original Stack
- Play Framework 2.1.0
- Java (older version)
- SBT build tool
- MySQL database

### New Stack
- Spring Boot 3.2.0
- Java 23
- Gradle build tool
- MySQL database (same as before)

## Key Changes

1. **Project Structure**:
   - Reorganized into standard Spring Boot package structure
   - Moved from SBT to Gradle build system

2. **Model Layer**:
   - Replaced Play model with JPA entity
   - Added Spring Data JPA repository
   - Added service layer for business logic

3. **Controller Layer**:
   - Replaced Play controller with Spring RestController
   - Used Spring's RequestBody and ResponseEntity

4. **View Layer**:
   - Replaced Play Scala templates with Jackson XML serialization/deserialization
   - Created DTOs for request/response handling

5. **Configuration**:
   - Replaced Play application.conf with Spring application.properties
   - Configured database connection, logging, etc.

6. **Testing**:
   - Replaced Play tests with Spring Boot tests
   - Added unit tests for repository, service, and controller layers
   - Used H2 in-memory database for testing

## Running the Application

1. Make sure you have Java 23 installed
2. Make sure you have MySQL running with a database named "qiwi"
3. Run the application:
   ```
   ./gradlew bootRun
   ```

## Testing the Application

Run the tests:
```
./gradlew test
```