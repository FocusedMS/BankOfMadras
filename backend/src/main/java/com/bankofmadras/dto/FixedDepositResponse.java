package com.bankofmadras.dto;

import com.bankofmadras.model.FixedDeposit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FixedDepositResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private LocalDateTime startDate;
    private LocalDateTime maturityDate;
    private boolean active;
    private LocalDateTime closedDate;
    private LocalDateTime createdAt;

    public static FixedDepositResponse fromEntity(FixedDeposit fixedDeposit) {
        FixedDepositResponse response = new FixedDepositResponse();
        response.setId(fixedDeposit.getId());
        response.setAccountNumber(fixedDeposit.getAccount().getAccountNumber());
        response.setAmount(fixedDeposit.getAmount());
        response.setInterestRate(fixedDeposit.getInterestRate());
        response.setStartDate(fixedDeposit.getStartDate());
        response.setMaturityDate(fixedDeposit.getMaturityDate());
        response.setActive(fixedDeposit.isActive());
        response.setClosedDate(fixedDeposit.getClosedDate());
        response.setCreatedAt(fixedDeposit.getCreatedAt());
        return response;
    }
} 