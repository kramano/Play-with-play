package com.example.qiwitest.integration;

import com.example.qiwitest.model.Client;
import com.example.qiwitest.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

/**
 * Integration tests for the ClientRepository using TestContainers.
 * These tests verify that the repository can interact with a real PostgreSQL database.
 */
public class ClientRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    public void shouldCreateAndFindClient() {
        // Create a client
        Client client = new Client("integration-test-user", "password", BigDecimal.ZERO);

        // Save the client and then find it by login
        StepVerifier.create(clientRepository.save(client)
                .then(clientRepository.findByLogin("integration-test-user")))
            .assertNext(found -> {
                // Verify the client was found and has the correct properties
                assert found.getLogin().equals("integration-test-user");
                assert found.getPassword().equals("password");
                assert found.getBalance().compareTo(BigDecimal.ZERO) == 0;
            })
            .verifyComplete();
    }

    @Test
    public void shouldUpdateClientBalance() {
        // Create a client
        Client client = new Client("balance-test-user", "password", BigDecimal.ZERO);

        // Save the client, update the balance, and verify the update
        StepVerifier.create(clientRepository.save(client)
                .flatMap(saved -> {
                    saved.setBalance(new BigDecimal("100.0000"));
                    return clientRepository.save(saved);
                })
                .then(clientRepository.findByLogin("balance-test-user")))
            .assertNext(found -> {
                // Verify the client was found and has the updated balance
                assert found.getLogin().equals("balance-test-user");
                assert found.getBalance().compareTo(new BigDecimal("100.0000")) == 0;
            })
            .verifyComplete();
    }

    @Test
    public void shouldDeleteClient() {
        // Create a client
        Client client = new Client("delete-test-user", "password", BigDecimal.ZERO);

        // Save the client, delete it, and verify it's gone
        StepVerifier.create(clientRepository.save(client)
                .flatMap(saved -> clientRepository.delete(saved))
                .then(clientRepository.findByLogin("delete-test-user")))
            .verifyComplete(); // Empty Mono completes without emitting any value
    }

    @Test
    public void shouldNotFindNonExistentClient() {
        // Try to find a client that doesn't exist
        Mono<Client> notFound = clientRepository.findByLogin("non-existent-user");

        // Verify the client was not found
        StepVerifier.create(notFound)
            .verifyComplete(); // Empty Mono completes without emitting any value
    }
}