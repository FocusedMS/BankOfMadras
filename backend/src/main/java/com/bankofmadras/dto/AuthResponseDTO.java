package com.bankofmadras.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String message;
    private String accountNumber;
    private String temporaryPassword;
    private String email;
    private String mobile;
    private BigDecimal balance;
    private String accessToken;
    private String refreshToken;
    private String role;
} 