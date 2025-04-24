package com.bankofmadras.service.impl;

import com.bankofmadras.model.Account;
import com.bankofmadras.model.AuditLog;
import com.bankofmadras.repository.AuditLogRepository;
import com.bankofmadras.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Override
    public AuditLog logAction(Account account, AuditLog.AuditAction action, String accountNumber, String description) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAccount(account);
        auditLog.setAction(action);
        auditLog.setAccountNumber(accountNumber);
        auditLog.setDescription(description);
        return auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> getAccountAuditLogs(Account account) {
        return auditLogRepository.findByAccount(account);
    }

    @Override
    public List<AuditLog> getAuditLogsByAction(AuditLog.AuditAction action) {
        return auditLogRepository.findByAction(action);
    }
} 