package com.example.qiwitest.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the API controller using TestContainers.
 * These tests verify that the API endpoints work correctly with a real PostgreSQL database.
 */
@AutoConfigureWebTestClient
public class ApiControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void shouldCreateNewClientAndGetBalance() {
        // First, create a new client
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(createClientXml("integration-api-user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("0");

        // Then, get the balance for the client
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(getBalanceXml("integration-api-user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("0")
                .xpath("/response/extra/extra[@name='balance']").isEqualTo("0.0000");
    }

    @Test
    public void shouldNotCreateDuplicateClient() {
        // First, create a new client
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(createClientXml("duplicate-user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("0");

        // Then, try to create the same client again
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(createClientXml("duplicate-user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("1"); // Client already exists
    }

    @Test
    public void shouldReturnErrorForNonExistentClient() {
        // Try to get balance for a non-existent client
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(getBalanceXml("non-existent-user", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("3"); // Client does not exist
    }

    @Test
    public void shouldReturnErrorForWrongPassword() {
        // First, create a new client
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(createClientXml("password-test-user", "correct-password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("0");

        // Then, try to get balance with wrong password
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(getBalanceXml("password-test-user", "wrong-password"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("4"); // Wrong password
    }

    private String createClientXml(String login, String password) {
        return String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                        <request-type>CREATE-AGT</request-type>
                        <extra name="login">%s</extra>
                        <extra name="password">%s</extra>
                </request>""", login, password);
    }

    private String getBalanceXml(String login, String password) {
        return String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                        <request-type>GET-BALANCE</request-type>
                        <extra name="login">%s</extra>
                        <extra name="password">%s</extra>
                </request>""", login, password);
    }
}