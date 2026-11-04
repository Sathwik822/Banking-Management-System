package com.bank.controller;

import com.bank.dto.Dto.*;
import com.bank.service.BankService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final BankService bank;
    public TransactionController(BankService bank) { this.bank = bank; }

    @PostMapping("/deposit")
    public AccountResponse deposit(Authentication auth, @Valid @RequestBody AmountRequest req) {
        return bank.deposit(auth.getName(), req);
    }

    @PostMapping("/withdraw")
    public AccountResponse withdraw(Authentication auth, @Valid @RequestBody AmountRequest req) {
        return bank.withdraw(auth.getName(), req);
    }

    @PostMapping("/transfer")
    public AccountResponse transfer(Authentication auth, @Valid @RequestBody TransferRequest req) {
        return bank.transfer(auth.getName(), req);
    }
}
