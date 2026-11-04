package com.bank.controller;

import com.bank.dto.Dto.*;
import com.bank.service.BankService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** Only reachable with ROLE_ADMIN (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final BankService bank;
    public AdminController(BankService bank) { this.bank = bank; }

    @GetMapping("/users")
    public List<UserResponse> users() { return bank.allUsers(); }

    @GetMapping("/accounts")
    public List<AdminAccountResponse> accounts() { return bank.allAccounts(); }
}
