package com.example.qiwitest.repository;

import com.example.qiwitest.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    public void shouldCreateAndFindClient() {
        // Create a client
        Client client = new Client("max", "pwd", BigDecimal.ZERO);
        entityManager.persist(client);
        entityManager.flush();

        // Find the client by login
        Optional<Client> found = clientRepository.findByLogin("max");
        
        // Verify the client was found and has the correct properties
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("max");
        assertThat(found.get().getPassword()).isEqualTo("pwd");
        assertThat(found.get().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void shouldNotFindClientInEmptyTable() {
        // Try to find a client that doesn't exist
        Optional<Client> notFound = clientRepository.findByLogin("max");
        
        // Verify the client was not found
        assertThat(notFound).isEmpty();
    }
}