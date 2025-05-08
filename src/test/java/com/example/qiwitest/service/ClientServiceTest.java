package com.example.qiwitest.service;

import com.example.qiwitest.model.Client;
import com.example.qiwitest.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    public void setUp() {
        testClient = new Client("max", "pwd", BigDecimal.ZERO);
    }

    @Test
    public void shouldFindClientByLogin() {
        // Arrange
        when(clientRepository.findByLogin("max")).thenReturn(Mono.just(testClient));

        // Act & Assert
        StepVerifier.create(clientService.findByLogin("max"))
            .expectNext(testClient)
            .verifyComplete();

        verify(clientRepository, times(1)).findByLogin("max");
    }

    @Test
    public void shouldReturnEmptyMonoWhenClientNotFound() {
        // Arrange
        when(clientRepository.findByLogin("nonexistent")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(clientService.findByLogin("nonexistent"))
            .verifyComplete();

        verify(clientRepository, times(1)).findByLogin("nonexistent");
    }

    @Test
    public void shouldCreateClient() {
        // Arrange
        when(clientRepository.save(any(Client.class))).thenReturn(Mono.just(testClient));

        // Act & Assert
        StepVerifier.create(clientService.createClient("max", "pwd"))
            .expectNext(testClient)
            .verifyComplete();

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void shouldCheckPasswordCorrectly() {
        // Arrange
        Mono<Client> clientMono = Mono.just(testClient);
        Mono<Client> emptyMono = Mono.empty();

        // Act & Assert
        StepVerifier.create(clientService.isPasswordCorrect(clientMono, "pwd"))
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(clientService.isPasswordCorrect(clientMono, "wrong"))
            .expectNext(false)
            .verifyComplete();

        StepVerifier.create(clientService.isPasswordCorrect(emptyMono, "pwd"))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void shouldGetBalance() {
        // Arrange
        Mono<Client> clientMono = Mono.just(testClient);
        Mono<Client> emptyMono = Mono.empty();

        // Act & Assert
        StepVerifier.create(clientService.getBalance(clientMono))
            .expectNext(BigDecimal.ZERO)
            .verifyComplete();

        StepVerifier.create(clientService.getBalance(emptyMono))
            .expectComplete()
            .verify();
    }
}
