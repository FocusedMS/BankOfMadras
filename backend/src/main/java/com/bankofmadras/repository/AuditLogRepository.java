package com.bankofmadras.repository;

import com.bankofmadras.model.Account;
import com.bankofmadras.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAccountOrderByTimestampDesc(Account account);
    List<AuditLog> findByActionOrderByTimestampDesc(AuditLog.AuditAction action);
    List<AuditLog> findByAdminAccountNumber(String adminAccountNumber);
    List<AuditLog> findByTargetUser(String targetUser);
    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
} 