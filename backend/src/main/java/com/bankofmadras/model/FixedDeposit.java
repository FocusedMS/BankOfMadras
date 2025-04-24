package com.bankofmadras.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fixed_deposits")
@EntityListeners(AuditingEntityListener.class)
public class FixedDeposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal maturityAmount;

    @Column(nullable = false)
    private Integer durationMonths;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FixedDepositStatus status;

    @Column(length = 500)
    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime closedDate;

    public boolean isActive() {
        return status == FixedDepositStatus.ACTIVE;
    }

    public enum FixedDepositStatus {
        ACTIVE,
        MATURED,
        CLOSED
    }
} 