package com.example.qiwitest.service;

import com.example.qiwitest.model.Client;
import com.example.qiwitest.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

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
     * @return the client if found, null otherwise
     */
    public Client findByLogin(String login) {
        try {
            return clientRepository.findByLogin(login).orElse(null);
        } catch (Exception e) {
            logger.error("Error finding client by login: {}", login, e);
            throw e;
        }
    }
    
    /**
     * Create a new client
     * 
     * @param login the client login
     * @param password the client password
     * @return the created client
     */
    @Transactional
    public Client createClient(String login, String password) {
        try {
            Client client = new Client(login, password);
            return clientRepository.save(client);
        } catch (Exception e) {
            logger.error("Error creating client with login: {}", login, e);
            throw e;
        }
    }
    
    /**
     * Check if the password is correct for the given client
     * 
     * @param client the client
     * @param password the password to check
     * @return true if the password is correct, false otherwise
     */
    public boolean isPasswordCorrect(Client client, String password) {
        return client != null && password.equals(client.getPassword());
    }
    
    /**
     * Get the balance for the given client
     * 
     * @param client the client
     * @return the client's balance
     */
    public BigDecimal getBalance(Client client) {
        return client != null ? client.getBalance() : null;
    }
}