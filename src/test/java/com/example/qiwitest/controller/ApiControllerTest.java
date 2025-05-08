package com.example.qiwitest.controller;

import com.example.qiwitest.model.Client;
import com.example.qiwitest.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ApiController.class)
@Import(ApiControllerTest.TestConfig.class)
public class ApiControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ClientService clientService() {
            return Mockito.mock(ClientService.class);
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    public void setUp() {
        testClient = new Client("123456", "pwd", new BigDecimal("0.0000"));
    }

    @Test
    public void shouldCreateNewClientSuccess() {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(Mono.empty());
        when(clientService.createClient("123456", "pwd")).thenReturn(Mono.just(testClient));

        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(createClientXml())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("0");
    }

    @Test
    public void shouldNotCreateNewClientWithExistingLogin() {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(Mono.just(testClient));

        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(createClientXml())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("1");
    }

    @Test
    public void shouldReturnBalanceForExistingClient() {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(Mono.just(testClient));
        when(clientService.isPasswordCorrect(any(Mono.class), any(String.class))).thenReturn(Mono.just(true));
        when(clientService.getBalance(any(Mono.class))).thenReturn(Mono.just(new BigDecimal("0.0000")));

        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(getBalanceXml())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("0")
                .xpath("/response/extra/extra[@name='balance']").isEqualTo("0.0000");
    }

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
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("3");
    }

    @Test
    public void shouldReturnErrorForWrongPassword() {
        // Arrange
        when(clientService.findByLogin("123456")).thenReturn(Mono.just(testClient));
        when(clientService.isPasswordCorrect(any(Mono.class), any(String.class))).thenReturn(Mono.just(false));

        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(getBalanceXml())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_XML)
                .expectBody()
                .xpath("/response/result-code").isEqualTo("4");
    }

    @Test
    public void shouldReturnErrorForUnknownType() {
        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(unknownRequestXml())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void shouldReturnErrorForMissingParameter() {
        // Act & Assert
        webTestClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_XML)
                .bodyValue(missingParamXml())
                .exchange()
                .expectStatus().isBadRequest();
    }

    private String createClientXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                        <request-type>CREATE-AGT</request-type>
                        <extra name="login">123456</extra>
                        <extra name="password">pwd</extra>
                </request>""";
    }

    private String getBalanceXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                        <request-type>GET-BALANCE</request-type>
                        <extra name="login">123456</extra>
                        <extra name="password">pwd</extra>
                </request>""";
    }

    private String unknownRequestXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                        <request-type>UNKNOWN</request-type>
                        <extra name="login">123456</extra>
                        <extra name="password">pwd</extra>
                </request>""";
    }

    private String missingParamXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                        <extra name="login">123456</extra>
                        <extra name="password">pwd</extra>
                </request>""";
    }
}
