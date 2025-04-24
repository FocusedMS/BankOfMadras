package com.bankofmadras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FixedDepositRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum fixed deposit amount is 1000")
    private BigDecimal amount;

    @NotNull(message = "Duration in months is required")
    private Integer durationInMonths;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
} 