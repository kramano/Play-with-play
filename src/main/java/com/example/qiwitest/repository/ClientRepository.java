package com.example.qiwitest.repository;

import com.example.qiwitest.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * Find a client by login
     * 
     * @param login the login to search for
     * @return the client if found, empty otherwise
     */
    Optional<Client> findByLogin(String login);
}