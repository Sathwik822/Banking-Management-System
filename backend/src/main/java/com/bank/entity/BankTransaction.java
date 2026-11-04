package com.bank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class BankTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public BankTransaction() {}
    public BankTransaction(Account account, String type, BigDecimal amount, BigDecimal balanceAfter, String description) {
        this.account = account; this.type = type; this.amount = amount;
        this.balanceAfter = balanceAfter; this.description = description;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getDescription() { return description; }
    public Account getAccount() { return account; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
