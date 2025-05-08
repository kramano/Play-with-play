package com.example.qiwitest.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CLIENTS")
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String login;
    
    @Column(nullable = false)
    private String password;
    
    @Column
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