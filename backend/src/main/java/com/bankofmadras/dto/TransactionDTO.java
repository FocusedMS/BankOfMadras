package com.bankofmadras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDTO {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum amount is 1")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    private String toAccountNumber; // For transfers only
} 