package com.example.qiwitest.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Table("CLIENTS")
public class Client {

    @Id
    private Long id;

    private String login;

    private String password;

    private BigDecimal balance = BigDecimal.ZERO;

    // Default constructor required by JPA
    public Client() {
    }

    public Client(String login, String password) {
        this.login = login;
        this.password = password;
        this.balance = BigDecimal.ZERO;
    }

    public Client(String login, String password, BigDecimal balance) {
        this.login = login;
        this.password = password;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
