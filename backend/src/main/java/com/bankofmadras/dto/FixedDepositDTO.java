package com.bankofmadras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FixedDepositDTO {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum amount is 1000")
    private BigDecimal amount;

    @NotNull(message = "Duration is required")
    @Min(value = 3, message = "Minimum duration is 3 months")
    private Integer durationMonths;

    @NotBlank(message = "Description is required")
    private String description;
} 