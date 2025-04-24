package com.bankofmadras.service;

import com.bankofmadras.dto.*;
import com.bankofmadras.model.Account;
import com.bankofmadras.repository.AccountRepository;
import com.bankofmadras.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TwilioService twilioService;
    private final OTPService otpService;
    private final JavaMailSender mailSender;

    @Transactional
    public AuthResponseDTO requestOTP(OTPRequestDTO request) {
        // Check if account already exists
        if (accountRepository.existsByEmail(request.getEmail()) || 
            accountRepository.existsByMobile(request.getMobile())) {
            throw new RuntimeException("Account already exists with this email or mobile");
        }

        // Generate and send OTP
        String otp = otpService.generateAndStoreOTP(request.getEmail());
        twilioService.sendOTP(request.getMobile(), otp);

        // Send email notification
        sendOTPEmail(request.getEmail(), otp);

        return new AuthResponseDTO("OTP sent to registered mobile/email.", null, null, 
            request.getEmail(), request.getMobile(), BigDecimal.ZERO, null, null, null);
    }

    @Transactional
    public AuthResponseDTO verifyOTP(OTPVerificationDTO request) {
        // Verify OTP
        if (!otpService.verifyOTP(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Generate account number
        String accountNumber = generateAccountNumber();

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Create account
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setEmail(request.getEmail());
        account.setPassword(passwordEncoder.encode(temporaryPassword));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account.setRole(Account.AccountRole.USER);

        accountRepository.save(account);

        // Send account creation email
        sendAccountCreationEmail(account, temporaryPassword);

        return new AuthResponseDTO(
            "Account created successfully.",
            accountNumber,
            temporaryPassword,
            request.getEmail(),
            null,
            BigDecimal.ZERO,
            null,
            null,
            "USER"
        );
    }

    public AuthResponseDTO login(LoginDTO request) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new RuntimeException("Invalid account number"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(account);
        String refreshToken = jwtTokenProvider.generateRefreshToken(account);

        return new AuthResponseDTO(
            "Login successful",
            account.getAccountNumber(),
            null,
            account.getEmail(),
            account.getMobile(),
            account.getBalance(),
            accessToken,
            refreshToken,
            account.getRole().name()
        );
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String accountNumber = jwtTokenProvider.getAccountNumberFromToken(refreshToken);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(account);

        return new AuthResponseDTO(
            "Token refreshed successfully",
            account.getAccountNumber(),
            null,
            account.getEmail(),
            account.getMobile(),
            account.getBalance(),
            newAccessToken,
            refreshToken,
            account.getRole().name()
        );
    }

    @Transactional
    public AuthResponseDTO deleteAccount(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }

        String accountNumber = jwtTokenProvider.getAccountNumberFromToken(token);
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot delete account with non-zero balance");
        }

        account.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(account);

        // Send account deletion email
        sendAccountDeletionEmail(account);

        return new AuthResponseDTO(
            "Account successfully deleted.",
            account.getAccountNumber(),
            null,
            account.getEmail(),
            account.getMobile(),
            account.getBalance(),
            null,
            null,
            account.getRole().name()
        );
    }

    @Transactional
    public AuthResponseDTO requestPasswordReset(PasswordResetRequestDTO request) {
        Account account = accountRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Account not found with this email"));

        // Generate and send OTP
        String otp = otpService.generateAndStoreOTP(request.getEmail());
        twilioService.sendOTP(account.getMobile(), otp);

        // Send email notification
        sendPasswordResetEmail(request.getEmail(), otp);

        return new AuthResponseDTO(
            "Password reset OTP sent to registered mobile/email.",
            account.getAccountNumber(),
            null,
            request.getEmail(),
            account.getMobile(),
            account.getBalance(),
            null,
            null,
            account.getRole().name()
        );
    }

    @Transactional
    public AuthResponseDTO verifyPasswordReset(PasswordResetVerificationDTO request) {
        // Verify OTP
        if (!otpService.verifyOTP(request.getEmail(), request.getOtp())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        Account account = accountRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Account not found"));

        // Update password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        // Send password change confirmation email
        sendPasswordChangeConfirmationEmail(account);

        return new AuthResponseDTO(
            "Password reset successful.",
            account.getAccountNumber(),
            null,
            account.getEmail(),
            account.getMobile(),
            account.getBalance(),
            null,
            null,
            account.getRole().name()
        );
    }

    private String generateAccountNumber() {
        Random random = new Random();
        // Generate 7 random digits
        int number = 1000000 + random.nextInt(9000000);
        // Format: BOM + 7 digits = 10 characters total
        return "BOM" + String.format("%07d", number);
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void sendOTPEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("OTP for Account Registration - Bank of Madras");
        message.setText(String.format(
            "Dear User,\n\n" +
            "Your OTP for account registration is: %s\n" +
            "This OTP will expire in 5 minutes.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            otp
        ));
        mailSender.send(message);
    }

    private void sendAccountCreationEmail(Account account, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(account.getEmail());
        message.setSubject("Account Created - Bank of Madras");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Your account has been created successfully.\n" +
            "Account Number: %s\n" +
            "Temporary Password: %s\n\n" +
            "Please change your password after first login.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            account.getAccountHolderName(),
            account.getAccountNumber(),
            temporaryPassword
        ));
        mailSender.send(message);
    }

    private void sendAccountDeletionEmail(Account account) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(account.getEmail());
        message.setSubject("Account Deactivated - Bank of Madras");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Your account has been deactivated.\n" +
            "If you believe this is an error, please contact customer support.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            account.getAccountHolderName()
        ));
        mailSender.send(message);
    }

    private void sendPasswordResetEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset OTP - Bank of Madras");
        message.setText(String.format(
            "Dear User,\n\n" +
            "Your OTP for password reset is: %s\n" +
            "This OTP will expire in 5 minutes.\n\n" +
            "If you did not request this password reset, please contact our support immediately.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            otp
        ));
        mailSender.send(message);
    }

    private void sendPasswordChangeConfirmationEmail(Account account) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(account.getEmail());
        message.setSubject("Password Changed Successfully - Bank of Madras");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Your password has been successfully changed.\n" +
            "If you did not make this change, please contact our support immediately.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            account.getAccountNumber()
        ));
        mailSender.send(message);
    }
} 