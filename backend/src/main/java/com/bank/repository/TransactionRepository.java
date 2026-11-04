package com.bank.repository;

import com.bank.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
