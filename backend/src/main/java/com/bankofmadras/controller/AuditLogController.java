package com.bankofmadras.controller;

import com.bankofmadras.model.AuditLog;
import com.bankofmadras.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Log Management", description = "APIs for managing audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get audit logs for an account")
    @PreAuthorize("hasRole('ADMIN') or @accountService.getAccountEntityByAccountNumber(#accountNumber).email == authentication.principal.username")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAccount(
            @PathVariable String accountNumber,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByAccount(accountNumber, pageable));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByDateRange(startDate, endDate));
    }

    @GetMapping("/action-type/{actionType}")
    @Operation(summary = "Get audit logs by action type (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getAuditLogsByActionType(@PathVariable AuditLog.ActionType actionType) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByActionType(actionType));
    }

    @GetMapping("/admin/{adminAccountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getLogsByAdmin(@PathVariable String adminAccountNumber) {
        return ResponseEntity.ok(auditLogService.getLogsByAdmin(adminAccountNumber));
    }

    @GetMapping("/user/{targetUser}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getLogsByTargetUser(@PathVariable String targetUser) {
        return ResponseEntity.ok(auditLogService.getLogsByTargetUser(targetUser));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditLogService.getLogsByDateRange(startDate, endDate));
    }
} 