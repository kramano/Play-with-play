package models;

import org.junit.Before;
import org.junit.Test;
import play.db.DB;
import play.test.WithApplication;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

/**
 * Author: Maxim Novik
 */
public class ClientTest extends WithApplication {

    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
        Connection connection = DB.getConnection();
        PreparedStatement createClients = null;
        try {
            createClients =
                    connection.prepareStatement("CREATE TABLE CLIENTS (\n" +
                            "ID BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                            "LOGIN VARCHAR(255) UNIQUE,\n" +
                            "PASSWORD VARCHAR(255) NOT NULL,\n" +
                            "BALANCE DECIMAL(19, 4) DEFAULT 0 \n" +
                            ");");
            createClients.executeUpdate();
            createClients.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void shouldCreateAndFindClient() throws Exception{
        new Client("max","pwd", BigDecimal.ZERO).save();
        Client max = Client.findByLogin("max");
        assertThat(max).isNotNull();
        assertThat(max.getPassword()).isEqualTo("pwd");
        assertThat(max.getLogin()).isEqualTo("max");
        assertThat(max.getBalance()).isZero();
    }

    @Test
    public void shouldNotFindClientInEmptyTable() throws Exception{
        Client notFound = Client.findByLogin("max");
        assertThat(notFound).isNull();
    }

}
