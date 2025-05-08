# Qiwi Test Application

This application provides a simple client management system with balance tracking functionality. It exposes a RESTful API that accepts and returns XML data.

## Functionality

### API Endpoints

The application exposes a single endpoint:

- **POST /** - Processes all client operations

### Operations

The API supports the following operations:

1. **CREATE-AGT** - Creates a new client account
   - Required parameters: `login`, `password`
   - Response: Result code indicating success or failure

2. **GET-BALANCE** - Retrieves the balance for a client
   - Required parameters: `login`, `password`
   - Response: Result code and balance (if successful)

### Result Codes

- **0** - Success
- **1** - Client already exists (for CREATE-AGT)
- **2** - Technical error
- **3** - Client does not exist
- **4** - Wrong password

### Request/Response Format

All requests and responses use XML format.

#### Request Format

```xml
<?xml version="1.0" encoding="UTF-8"?>
<request>
    <request-type>OPERATION_TYPE</request-type>
    <extra name="login">LOGIN_VALUE</extra>
    <extra name="password">PASSWORD_VALUE</extra>
</request>
```

#### Response Format

```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
    <result-code>RESULT_CODE</result-code>
    <extra name="balance">BALANCE_VALUE</extra> <!-- Only for GET-BALANCE -->
</response>
```

### Examples

#### Creating a New Client

Request:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<request>
    <request-type>CREATE-AGT</request-type>
    <extra name="login">user123</extra>
    <extra name="password">securepass</extra>
</request>
```

Success Response:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
    <result-code>0</result-code>
</response>
```

#### Getting Client Balance

Request:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<request>
    <request-type>GET-BALANCE</request-type>
    <extra name="login">user123</extra>
    <extra name="password">securepass</extra>
</request>
```

Success Response:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
    <result-code>0</result-code>
    <extra name="balance">0.0000</extra>
</response>
```

## Migration Details

### Original Stack
- Play Framework 2.1.0
- Java (older version)
- SBT build tool
- MySQL database

### New Stack
- Spring Boot 3.4.5
- Java 23
- Gradle build tool
- PostgreSQL database
- Spring WebFlux (reactive programming)
- R2DBC (reactive database connectivity)

## Key Changes

1. **Project Structure**:
   - Reorganized into standard Spring Boot package structure
   - Moved from SBT to Gradle build system

2. **Model Layer**:
   - Replaced Play model with R2DBC entity
   - Added Spring Data R2DBC repository
   - Added reactive service layer for business logic

3. **Controller Layer**:
   - Replaced Play controller with Spring WebFlux controller
   - Used reactive programming model with Mono/Flux
   - Used Spring's RequestBody and ResponseEntity with reactive types

4. **View Layer**:
   - Replaced Play Scala templates with Jackson XML serialization/deserialization
   - Created DTOs for request/response handling

5. **Configuration**:
   - Replaced Play application.conf with Spring application.properties
   - Configured database connection, logging, etc.

6. **Testing**:
   - Replaced Play tests with Spring Boot WebFlux tests
   - Added reactive unit tests for repository, service, and controller layers
   - Used H2 in-memory database with R2DBC for testing
   - Used StepVerifier for testing reactive streams

## Running the Application

1. Make sure you have Java 23 installed
2. Use Docker Compose to start the application and PostgreSQL database:
   ```
   docker-compose up
   ```

   Or run locally:
   ```
   ./gradlew bootRun
   ```
   (requires PostgreSQL running with a database named "qiwi")

## Testing the Application

Run the tests:
```
./gradlew test
```

### Integration Tests with TestContainers

The application includes integration tests that use TestContainers to spin up a real PostgreSQL database in a Docker container during test execution. This ensures that the tests run against the same database technology used in production.

Integration tests are available for:

1. **Repository Layer**: Tests CRUD operations against a real PostgreSQL database
   - Creating and finding clients
   - Updating client balances
   - Deleting clients

2. **API Endpoints**: End-to-end tests for the REST API
   - Creating new clients
   - Getting client balances
   - Handling error cases (duplicate clients, wrong passwords, etc.)

To run the integration tests:
```
./gradlew test
```

The integration tests will automatically:
1. Start a PostgreSQL container
2. Initialize the database schema using the create.sql script
3. Run the tests against the containerized database
4. Shut down the container when tests complete

Requirements:
- Docker must be installed and running on your machine
- Your user must have permissions to create Docker containers

Note: If Docker is not available, the integration tests will be automatically skipped rather than failing. This allows the build to succeed even in environments without Docker.
