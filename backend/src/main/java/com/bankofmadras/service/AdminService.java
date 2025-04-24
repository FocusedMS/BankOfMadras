package com.bankofmadras.service;

import com.bankofmadras.model.Account;
import com.bankofmadras.model.AuditLog;
import com.bankofmadras.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final JavaMailSender mailSender;

    @Transactional
    public Account createAdmin(Account admin, String password, Account requestingAdmin) {
        // Check if requesting admin is a super admin
        if (requestingAdmin.getRole() != Account.AccountRole.SUPER_ADMIN) {
            throw new RuntimeException("Only super admins can create new admins");
        }

        // Generate admin ID (BOM + 7 digits)
        String adminId = generateAdminId();
        admin.setAccountNumber(adminId);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Account.AccountRole.ADMIN);
        admin.setStatus(Account.AccountStatus.ACTIVE);

        Account savedAdmin = accountRepository.save(admin);
        
        // Log the admin creation
        auditLogService.logAction(requestingAdmin, AuditLog.AuditAction.CREATED_ADMIN, adminId, 
                "Admin account created with ID: " + adminId);
        
        // Send email notification
        sendAdminCreationEmail(savedAdmin, password);
        
        return savedAdmin;
    }

    @Transactional
    public void deleteAdmin(String adminId, Account requestingAdmin) {
        Account adminToDelete = accountRepository.findByAccountNumber(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (adminToDelete.getRole() != Account.AccountRole.ADMIN) {
            throw new RuntimeException("Account is not an admin");
        }

        // Check if requesting admin is a super admin
        if (requestingAdmin.getRole() != Account.AccountRole.SUPER_ADMIN) {
            throw new RuntimeException("Only super admins can delete other admins");
        }

        adminToDelete.setStatus(Account.AccountStatus.INACTIVE);
        accountRepository.save(adminToDelete);

        // Log the admin deletion
        auditLogService.logAction(requestingAdmin, AuditLog.AuditAction.DELETED_ADMIN, adminId,
                "Admin account deactivated: " + adminId);

        // Send email notification
        sendAdminDeletionEmail(adminToDelete);
    }

    private String generateAdminId() {
        Random random = new Random();
        int number = 10000000 + random.nextInt(90000000);
        return "BOM" + String.format("%08d", number);
    }

    private void sendAdminCreationEmail(Account admin, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(admin.getEmail());
        message.setSubject("Admin Account Created - Bank of Madras");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Your admin account has been created successfully.\n" +
            "Account Number: %s\n" +
            "Temporary Password: %s\n\n" +
            "Please change your password after first login.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            admin.getAccountHolderName(),
            admin.getAccountNumber(),
            password
        ));
        mailSender.send(message);
    }

    private void sendAdminDeletionEmail(Account admin) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(admin.getEmail());
        message.setSubject("Admin Account Deactivated - Bank of Madras");
        message.setText(String.format(
            "Dear %s,\n\n" +
            "Your admin account has been deactivated.\n" +
            "If you believe this is an error, please contact the super admin.\n\n" +
            "Best regards,\n" +
            "Bank of Madras Team",
            admin.getAccountHolderName()
        ));
        mailSender.send(message);
    }
} 