package com.example.qiwitest.service;

import com.example.qiwitest.model.Client;
import com.example.qiwitest.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        when(clientRepository.findByLogin("max")).thenReturn(Optional.of(testClient));

        // Act
        Client found = clientService.findByLogin("max");

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getLogin()).isEqualTo("max");
        assertThat(found.getPassword()).isEqualTo("pwd");
        verify(clientRepository, times(1)).findByLogin("max");
    }

    @Test
    public void shouldReturnNullWhenClientNotFound() {
        // Arrange
        when(clientRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        // Act
        Client found = clientService.findByLogin("nonexistent");

        // Assert
        assertThat(found).isNull();
        verify(clientRepository, times(1)).findByLogin("nonexistent");
    }

    @Test
    public void shouldCreateClient() {
        // Arrange
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        // Act
        Client created = clientService.createClient("max", "pwd");

        // Assert
        assertThat(created).isNotNull();
        assertThat(created.getLogin()).isEqualTo("max");
        assertThat(created.getPassword()).isEqualTo("pwd");
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void shouldCheckPasswordCorrectly() {
        // Act & Assert
        assertThat(clientService.isPasswordCorrect(testClient, "pwd")).isTrue();
        assertThat(clientService.isPasswordCorrect(testClient, "wrong")).isFalse();
        assertThat(clientService.isPasswordCorrect(null, "pwd")).isFalse();
    }

    @Test
    public void shouldGetBalance() {
        // Act & Assert
        assertThat(clientService.getBalance(testClient)).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(clientService.getBalance(null)).isNull();
    }
}