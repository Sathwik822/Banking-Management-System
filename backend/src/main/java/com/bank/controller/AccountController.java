package com.bank.controller;

import com.bank.dto.Dto.*;
import com.bank.service.BankService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final BankService bank;
    public AccountController(BankService bank) { this.bank = bank; }

    @PostMapping
    public AccountResponse open(Authentication auth, @Valid @RequestBody OpenAccountRequest req) {
        return bank.openAccount(auth.getName(), req);
    }

    @GetMapping
    public List<AccountResponse> mine(Authentication auth) { return bank.myAccounts(auth.getName()); }

    @GetMapping("/{accountNumber}/transactions")
    public List<TransactionResponse> history(Authentication auth, @PathVariable String accountNumber) {
        return bank.history(auth.getName(), accountNumber);
    }
}
