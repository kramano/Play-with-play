package com.example.qiwitest.service;

import com.example.qiwitest.model.Client;
import com.example.qiwitest.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Find a client by login
     * 
     * @param login the login to search for
     * @return the client if found, empty Mono otherwise
     */
    public Mono<Client> findByLogin(String login) {
        return clientRepository.findByLogin(login)
            .doOnError(e -> logger.error("Error finding client by login: {}", login, e));
    }

    /**
     * Create a new client
     * 
     * @param login the client login
     * @param password the client password
     * @return the created client
     */
    @Transactional
    public Mono<Client> createClient(String login, String password) {
        return Mono.just(new Client(login, password))
            .flatMap(clientRepository::save)
            .doOnError(e -> logger.error("Error creating client with login: {}", login, e));
    }

    /**
     * Check if the password is correct for the given client
     * 
     * @param clientMono the client mono
     * @param password the password to check
     * @return true if the password is correct, false otherwise
     */
    public Mono<Boolean> isPasswordCorrect(Mono<Client> clientMono, String password) {
        return clientMono
            .map(client -> password.equals(client.getPassword()))
            .defaultIfEmpty(false);
    }

    /**
     * Get the balance for the given client
     * 
     * @param clientMono the client mono
     * @return the client's balance or empty Mono if client not found
     */
    public Mono<BigDecimal> getBalance(Mono<Client> clientMono) {
        return clientMono
            .map(Client::getBalance);
    }
}
