package com.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** All request / response objects in one place. */
public final class Dto {
    private Dto() {}

    // ----- requests -----
    public record RegisterRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record OpenAccountRequest(
            @NotBlank @Pattern(regexp = "SAVINGS|CURRENT", message = "Type must be SAVINGS or CURRENT") String accountType,
            @NotNull @DecimalMin(value = "0.00") BigDecimal initialDeposit) {}

    public record AmountRequest(
            @NotBlank String accountNumber,
            @NotNull @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount) {}

    public record TransferRequest(
            @NotBlank String fromAccount,
            @NotBlank String toAccount,
            @NotNull @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount) {}

    // ----- responses -----
    public record AuthResponse(String token, String fullName, String email, String role) {}

    public record AccountResponse(Long id, String accountNumber, String accountType,
                                  BigDecimal balance, LocalDateTime createdAt) {}

    public record TransactionResponse(Long id, String type, BigDecimal amount, BigDecimal balanceAfter,
                                      String description, LocalDateTime createdAt) {}

    public record UserResponse(Long id, String fullName, String email, String role) {}

    public record AdminAccountResponse(String accountNumber, String accountType, BigDecimal balance, String ownerEmail) {}
}
