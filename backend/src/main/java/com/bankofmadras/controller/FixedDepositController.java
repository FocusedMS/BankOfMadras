package com.bankofmadras.controller;

import com.bankofmadras.dto.FixedDepositRequest;
import com.bankofmadras.dto.FixedDepositResponse;
import com.bankofmadras.service.FixedDepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fixed-deposits")
@RequiredArgsConstructor
@Tag(name = "Fixed Deposit Management", description = "APIs for managing fixed deposits")
public class FixedDepositController {

    private final FixedDepositService fixedDepositService;

    @PostMapping("/{accountNumber}")
    @Operation(summary = "Create a new fixed deposit")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<FixedDepositResponse> createFixedDeposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody FixedDepositRequest request) {
        return ResponseEntity.ok(fixedDepositService.createFixedDeposit(accountNumber, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fixed deposit details by ID")
    @PreAuthorize("hasRole('ADMIN') or @fixedDepositService.getFixedDeposit(#id).accountNumber == @accountService.getAccountEntityByEmail(authentication.principal.username).accountNumber")
    public ResponseEntity<FixedDepositResponse> getFixedDeposit(@PathVariable Long id) {
        return ResponseEntity.ok(fixedDepositService.getFixedDeposit(id));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get all fixed deposits for an account")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<List<FixedDepositResponse>> getFixedDepositsByAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(fixedDepositService.getFixedDepositsByAccount(accountNumber));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a fixed deposit")
    @PreAuthorize("hasRole('ADMIN') or @fixedDepositService.getFixedDeposit(#id).accountNumber == @accountService.getAccountEntityByEmail(authentication.principal.username).accountNumber")
    public ResponseEntity<Void> closeFixedDeposit(@PathVariable Long id) {
        fixedDepositService.closeFixedDeposit(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process-matured")
    @Operation(summary = "Process matured fixed deposits (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> processMaturedFixedDeposits() {
        fixedDepositService.processMaturedFixedDeposits();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createFixedDeposit(@Valid @RequestBody FixedDepositRequest dto) {
        FixedDepositResponse fixedDeposit = fixedDepositService.createFixedDeposit(dto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Fixed deposit created successfully.");
        response.put("fixedDepositId", fixedDeposit.getFixedDepositId());
        response.put("maturityAmount", fixedDeposit.getMaturityAmount());
        
        return ResponseEntity.ok(response);
    }
} 