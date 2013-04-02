package models;

import play.Logger;
import play.db.DB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Author: Maxim Novik
 */

public class Client {
    private String login;
    private String password;
    private BigDecimal balance;

    public Client(String login, String password, BigDecimal balance) {
        this.login = login;
        this.password = password;
        this.balance = balance;
    }

    public Client(String login, String password) {
        this(login, password, BigDecimal.ZERO);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void save() throws Exception {
        Connection connection = DB.getConnection();
        PreparedStatement createClient = null;
        try {
            createClient =
                    connection.prepareStatement("insert into clients(login, password) values(?, ?)");
            createClient.setString(1, login);
            createClient.setString(2, password);
            createClient.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Unable to save client: ", e);
            throw e;
        } finally {
            try {
                if (createClient != null) {
                    createClient.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                Logger.error("Unable to close database resources: ", e);
            }
        }
    }

    public static Client findByLogin(String login) throws Exception{
        Connection connection = DB.getConnection();
        PreparedStatement findByLogin = null;
        ResultSet resultSet = null;
        Client foundClient = null;
        try {
            findByLogin = connection.prepareStatement("select login, password, balance from Clients where login = ?");
            findByLogin.setString(1, login);
            resultSet = findByLogin.executeQuery();
            if (resultSet.isBeforeFirst()) {
                resultSet.first();
                String password = resultSet.getString("password");
                BigDecimal balance = resultSet.getBigDecimal("balance");
                foundClient = new Client(login, password, balance);
            }
        } catch (SQLException e) {
            Logger.error("Unable to find client: ", e);
            throw e;
        }  finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (findByLogin != null) {
                    findByLogin.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                Logger.error("Unable to close database resources: ", e);
            }
        }
        return foundClient;
    }
}
