package com.bankofmadras.controller;

import com.bankofmadras.dto.*;
import com.bankofmadras.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Request OTP for registration", description = "Sends OTP to the provided email and mobile number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Account already exists")
    })
    @PostMapping("/request-otp")
    public ResponseEntity<AuthResponseDTO> requestOTP(@Valid @RequestBody OTPRequestDTO request) {
        return ResponseEntity.ok(authService.requestOTP(request));
    }

    @Operation(summary = "Verify OTP and create account", description = "Verifies OTP and creates a new account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP"),
        @ApiResponse(responseCode = "404", description = "OTP not found")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOTP(@Valid @RequestBody OTPVerificationDTO request) {
        return ResponseEntity.ok(authService.verifyOTP(request));
    }

    @Operation(summary = "Login to account", description = "Authenticates user and returns JWT tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh access token", description = "Generates a new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @Parameter(description = "Refresh token in Authorization header", required = true)
            @RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "Request password reset", description = "Sends OTP for password reset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/request-password-reset")
    public ResponseEntity<AuthResponseDTO> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        return ResponseEntity.ok(authService.requestPasswordReset(request));
    }

    @Operation(summary = "Verify password reset", description = "Verifies OTP and resets password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successful"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/verify-password-reset")
    public ResponseEntity<AuthResponseDTO> verifyPasswordReset(@Valid @RequestBody PasswordResetVerificationDTO request) {
        return ResponseEntity.ok(authService.verifyPasswordReset(request));
    }

    @Operation(summary = "Delete account", description = "Deactivates the user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete account with non-zero balance"),
        @ApiResponse(responseCode = "401", description = "Invalid token"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/account/delete")
    public ResponseEntity<AuthResponseDTO> deleteAccount(
            @Parameter(description = "Access token in Authorization header", required = true)
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.deleteAccount(token));
    }
} 