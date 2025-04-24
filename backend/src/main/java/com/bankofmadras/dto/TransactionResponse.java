package com.bankofmadras.dto;

import com.bankofmadras.model.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private String description;
    private LocalDateTime timestamp;
    private Transaction.TransactionStatus status;

    public static TransactionResponse fromEntity(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAccountNumber(transaction.getAccount() != null ? transaction.getAccount().getAccountNumber() : null);
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setDescription(transaction.getDescription());
        response.setTimestamp(transaction.getTimestamp());
        response.setStatus(transaction.getStatus());
        return response;
    }
} 