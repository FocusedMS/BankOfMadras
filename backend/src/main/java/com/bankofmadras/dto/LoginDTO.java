package com.bankofmadras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^BOM[0-9]{7}$", message = "Invalid account number format")
    private String accountNumber;

    @NotBlank(message = "Password is required")
    private String password;
} 