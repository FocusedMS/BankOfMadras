package com.bankofmadras.controller;

import com.bankofmadras.dto.AccountRegistrationRequest;
import com.bankofmadras.dto.AccountResponse;
import com.bankofmadras.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<AccountResponse> registerAccount(
            @Valid @RequestBody AccountRegistrationRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(accountService.registerAccount(request));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details by account number")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(accountNumber));
    }

    @GetMapping
    @Operation(summary = "Get all accounts (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PutMapping("/{accountNumber}")
    @Operation(summary = "Update account details")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountRegistrationRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, request));
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Delete account")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountNumber}/block")
    @Operation(summary = "Block account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockAccount(@PathVariable String accountNumber) {
        accountService.blockAccount(accountNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountNumber}/unblock")
    @Operation(summary = "Unblock account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unblockAccount(@PathVariable String accountNumber) {
        accountService.unblockAccount(accountNumber);
        return ResponseEntity.ok().build();
    }
} 