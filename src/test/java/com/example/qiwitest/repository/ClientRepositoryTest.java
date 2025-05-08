package com.example.qiwitest.repository;

import com.example.qiwitest.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@DataR2dbcTest
@ActiveProfiles("test")
public class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    public void shouldCreateAndFindClient() {
        // Create a client
        Client client = new Client("max", "pwd", BigDecimal.ZERO);

        // Save the client and then find it by login
        StepVerifier.create(clientRepository.save(client)
                .then(clientRepository.findByLogin("max")))
            .assertNext(found -> {
                // Verify the client was found and has the correct properties
                assert found.getLogin().equals("max");
                assert found.getPassword().equals("pwd");
                assert found.getBalance().compareTo(BigDecimal.ZERO) == 0;
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotFindClientInEmptyTable() {
        // Try to find a client that doesn't exist
        Mono<Client> notFound = clientRepository.findByLogin("max");

        // Verify the client was not found
        StepVerifier.create(notFound)
            .verifyComplete(); // Empty Mono completes without emitting any value
    }
}
