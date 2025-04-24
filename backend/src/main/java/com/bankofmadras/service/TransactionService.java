package com.bankofmadras.service;

import com.bankofmadras.dto.TransactionDTO;
import com.bankofmadras.model.Account;
import com.bankofmadras.model.Transaction;
import com.bankofmadras.repository.AccountRepository;
import com.bankofmadras.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Transaction deposit(TransactionDTO request, Account account) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        // Update account balance
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription(request.getDescription());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the transaction
        auditLogService.logAction(account, AuditLog.AuditAction.DEPOSIT, 
            account.getAccountNumber(), "Deposit of " + request.getAmount());

        return savedTransaction;
    }

    @Transactional
    public Transaction withdraw(TransactionDTO request, Account account) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Update account balance
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setDescription(request.getDescription());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the transaction
        auditLogService.logAction(account, AuditLog.AuditAction.WITHDRAWAL, 
            account.getAccountNumber(), "Withdrawal of " + request.getAmount());

        return savedTransaction;
    }

    @Transactional
    public Transaction transfer(TransactionDTO request, Account fromAccount) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
            .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        // Update balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(fromAccount);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(request.getDescription());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setToAccount(toAccount);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the transaction
        auditLogService.logAction(fromAccount, AuditLog.AuditAction.TRANSFER, 
            fromAccount.getAccountNumber(), "Transfer of " + request.getAmount() + " to " + toAccount.getAccountNumber());

        return savedTransaction;
    }

    public Page<Transaction> getTransactionHistory(String accountNumber, Pageable pageable) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return transactionRepository.findByAccountAccountNumber(accountNumber, pageable);
    }

    public List<Transaction> getTransactionsByDateRange(String accountNumber, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByAccountAccountNumberAndCreatedAtBetween(accountNumber, startDate, endDate);
    }
} 