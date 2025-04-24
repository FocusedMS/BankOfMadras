package com.bankofmadras.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String accountNumber;

    @Column(length = 1000)
    private String description;

    @CreatedDate
    private LocalDateTime timestamp;

    public enum AuditAction {
        LOGIN,
        LOGOUT,
        PASSWORD_CHANGE,
        ACCOUNT_CREATION,
        ACCOUNT_DELETION,
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        CREATED_FD,
        FD_MATURED,
        FD_CLOSED,
        STATEMENT_GENERATED
    }
} 