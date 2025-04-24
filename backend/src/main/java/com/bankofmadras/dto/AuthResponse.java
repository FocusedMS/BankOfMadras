package com.bankofmadras.dto;

import com.bankofmadras.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String role;
    private String accountNumber;
    private String temporaryPassword;
    private String email;
    private String mobile;
    private Double balance;
} 