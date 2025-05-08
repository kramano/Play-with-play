package com.example.qiwitest.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using TestContainers.
 * This class sets up a PostgreSQL container for testing.
 * Tests will be skipped if Docker is not available.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractIntegrationTest.class);

    /**
     * Check if Docker is available and skip tests if it's not.
     * This method uses JUnit's Assumptions to skip tests when Docker is not available.
     */
    @BeforeAll
    public static void checkDockerAvailability() {
        try {
            DockerClientFactory.instance().client();
            logger.info("Docker is available, integration tests will run");
        } catch (Exception e) {
            logger.warn("Docker is not available, integration tests will be skipped: {}", e.getMessage());
            Assumptions.assumeTrue(false, "Docker is not available: " + e.getMessage());
        }
    }

    /**
     * PostgreSQL container for integration tests.
     * This container is shared among all test classes that extend this class.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("create.sql"); // This will run the create.sql script to initialize the database

    /**
     * Configure Spring Boot to use the TestContainers PostgreSQL instance.
     * This method sets the R2DBC connection properties dynamically.
     *
     * @param registry the property registry
     */
    @DynamicPropertySource
    static void registerR2dbcProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/%s",
                postgreSQLContainer.getHost(),
                postgreSQLContainer.getFirstMappedPort(),
                postgreSQLContainer.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
    }
}
