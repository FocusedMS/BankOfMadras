package com.bankofmadras.controller;

import com.bankofmadras.dto.TransactionDTO;
import com.bankofmadras.model.Transaction;
import com.bankofmadras.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing bank transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money into an account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deposit(@Valid @RequestBody TransactionDTO dto) {
        Transaction transaction = transactionService.deposit(dto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deposit successful.");
        response.put("balance", transaction.getBalanceAfter());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from an account")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#dto.accountNumber).email == authentication.principal.username")
    public ResponseEntity<Map<String, Object>> withdraw(@Valid @RequestBody TransactionDTO dto) {
        Transaction transaction = transactionService.withdraw(dto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Withdrawal successful.");
        response.put("balance", transaction.getBalanceAfter());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between accounts")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#dto.accountNumber).email == authentication.principal.username")
    public ResponseEntity<Map<String, Object>> transfer(@Valid @RequestBody TransactionDTO dto) {
        Transaction transaction = transactionService.transfer(dto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transfer successful.");
        response.put("fromNewBalance", transaction.getBalanceAfter());
        response.put("toNewBalance", transaction.getToAccount().getBalance());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{accountNumber}")
    @Operation(summary = "Get transaction history for an account")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountNumber,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber, pageable));
    }

    @GetMapping("/history/{accountNumber}/date-range")
    @Operation(summary = "Get transactions by date range")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionService.getTransactionsByDateRange(accountNumber, startDate, endDate));
    }
} 