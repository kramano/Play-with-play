package com.example.qiwitest.repository;

import com.example.qiwitest.model.Client;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {

    /**
     * Find a client by login
     * 
     * @param login the login to search for
     * @return the client if found, empty otherwise
     */
    Mono<Client> findByLogin(String login);
}
