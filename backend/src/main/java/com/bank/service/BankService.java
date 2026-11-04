package com.bank.service;

import com.bank.dto.Dto.*;
import com.bank.entity.Account;
import com.bank.entity.BankTransaction;
import com.bank.entity.User;
import com.bank.exception.BankException;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.List;

@Service
public class BankService {
    private final AccountRepository accounts;
    private final TransactionRepository txns;
    private final UserRepository users;
    private final SecureRandom random = new SecureRandom();

    public BankService(AccountRepository accounts, TransactionRepository txns, UserRepository users) {
        this.accounts = accounts; this.txns = txns; this.users = users;
    }

    // ---------- accounts ----------
    @Transactional
    public AccountResponse openAccount(String email, OpenAccountRequest req) {
        User owner = users.findByEmail(email)
                .orElseThrow(() -> new BankException("User not found", HttpStatus.NOT_FOUND));
        Account a = new Account();
        a.setAccountNumber(generateAccountNumber());
        a.setAccountType(req.accountType());
        a.setOwner(owner);
        BigDecimal initial = money(req.initialDeposit());
        a.setBalance(initial);
        a = accounts.save(a);
        if (initial.signum() > 0) {
            txns.save(new BankTransaction(a, "DEPOSIT", initial, initial, "Initial deposit"));
        }
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> myAccounts(String email) {
        return accounts.findByOwnerEmailOrderByCreatedAtDesc(email).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String email, String accountNumber) {
        Account a = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankException("Account not found", HttpStatus.NOT_FOUND));
        assertOwner(a, email);
        return txns.findByAccountIdOrderByCreatedAtDesc(a.getId()).stream()
                .map(t -> new TransactionResponse(t.getId(), t.getType(), t.getAmount(),
                        t.getBalanceAfter(), t.getDescription(), t.getCreatedAt()))
                .toList();
    }

    // ---------- money movement ----------
    @Transactional
    public AccountResponse deposit(String email, AmountRequest req) {
        Account a = lockOwned(req.accountNumber(), email);
        BigDecimal amt = money(req.amount());
        a.setBalance(a.getBalance().add(amt));
        accounts.save(a);
        txns.save(new BankTransaction(a, "DEPOSIT", amt, a.getBalance(), "Cash deposit"));
        return toResponse(a);
    }

    @Transactional
    public AccountResponse withdraw(String email, AmountRequest req) {
        Account a = lockOwned(req.accountNumber(), email);
        BigDecimal amt = money(req.amount());
        if (a.getBalance().compareTo(amt) < 0) {
            throw new BankException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }
        a.setBalance(a.getBalance().subtract(amt));
        accounts.save(a);
        txns.save(new BankTransaction(a, "WITHDRAW", amt, a.getBalance(), "Cash withdrawal"));
        return toResponse(a);
    }

    @Transactional
    public AccountResponse transfer(String email, TransferRequest req) {
        if (req.fromAccount().equals(req.toAccount())) {
            throw new BankException("Cannot transfer to the same account", HttpStatus.BAD_REQUEST);
        }
        BigDecimal amt = money(req.amount());

        // lock rows in a fixed order to avoid deadlocks
        String first = req.fromAccount().compareTo(req.toAccount()) < 0 ? req.fromAccount() : req.toAccount();
        String second = first.equals(req.fromAccount()) ? req.toAccount() : req.fromAccount();
        Account a1 = lock(first);
        Account a2 = lock(second);
        Account src = first.equals(req.fromAccount()) ? a1 : a2;
        Account dst = src == a1 ? a2 : a1;

        assertOwner(src, email);
        if (src.getBalance().compareTo(amt) < 0) {
            throw new BankException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }
        src.setBalance(src.getBalance().subtract(amt));
        dst.setBalance(dst.getBalance().add(amt));
        accounts.save(src);
        accounts.save(dst);
        txns.save(new BankTransaction(src, "TRANSFER_OUT", amt, src.getBalance(), "Transfer to " + dst.getAccountNumber()));
        txns.save(new BankTransaction(dst, "TRANSFER_IN", amt, dst.getBalance(), "Transfer from " + src.getAccountNumber()));
        return toResponse(src);
    }

    // ---------- admin ----------
    @Transactional(readOnly = true)
    public List<UserResponse> allUsers() {
        return users.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole())).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAccountResponse> allAccounts() {
        return accounts.findAll().stream()
                .map(a -> new AdminAccountResponse(a.getAccountNumber(), a.getAccountType(),
                        a.getBalance(), a.getOwner().getEmail())).toList();
    }

    // ---------- helpers ----------
    private Account lock(String number) {
        return accounts.findForUpdate(number)
                .orElseThrow(() -> new BankException("Account " + number + " not found", HttpStatus.NOT_FOUND));
    }

    private Account lockOwned(String number, String email) {
        Account a = lock(number);
        assertOwner(a, email);
        return a;
    }

    private void assertOwner(Account a, String email) {
        if (!a.getOwner().getEmail().equals(email)) {
            throw new BankException("You do not own this account", HttpStatus.FORBIDDEN);
        }
    }

    private BigDecimal money(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    private String generateAccountNumber() {
        String n;
        do {
            StringBuilder sb = new StringBuilder("AC");
            for (int i = 0; i < 10; i++) sb.append(random.nextInt(10));
            n = sb.toString();
        } while (accounts.existsByAccountNumber(n));
        return n;
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getAccountNumber(), a.getAccountType(), a.getBalance(), a.getCreatedAt());
    }
}
