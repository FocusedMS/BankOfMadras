package com.bankofmadras.service;

import com.bankofmadras.model.Account;
import com.bankofmadras.model.AuditLog;
import com.bankofmadras.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public void logAction(Account admin, AuditLog.AuditAction action, String targetUser, String details) {
        AuditLog log = new AuditLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setTargetUser(targetUser);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByAdmin(String adminAccountNumber) {
        return auditLogRepository.findByAdminAccountNumber(adminAccountNumber);
    }

    public List<AuditLog> getLogsByTargetUser(String targetUser) {
        return auditLogRepository.findByTargetUser(targetUser);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }
} 