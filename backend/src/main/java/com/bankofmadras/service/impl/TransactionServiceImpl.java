package com.bankofmadras.service.impl;

import com.bankofmadras.dto.TransactionDTO;
import com.bankofmadras.model.Account;
import com.bankofmadras.model.AuditLog;
import com.bankofmadras.model.Transaction;
import com.bankofmadras.repository.AccountRepository;
import com.bankofmadras.repository.TransactionRepository;
import com.bankofmadras.service.AuditLogService;
import com.bankofmadras.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public Transaction deposit(TransactionDTO request, Account account) {
        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription(request.getDescription());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // Update account balance
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        // Update transaction status
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the action
        auditLogService.logAction(account, AuditLog.AuditAction.DEPOSIT, 
            account.getAccountNumber(), "Deposit: " + request.getAmount());

        return savedTransaction;
    }

    @Override
    @Transactional
    public Transaction withdraw(TransactionDTO request, Account account) {
        // Check sufficient balance
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setDescription(request.getDescription());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // Update account balance
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        // Update transaction status
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the action
        auditLogService.logAction(account, AuditLog.AuditAction.WITHDRAWAL, 
            account.getAccountNumber(), "Withdrawal: " + request.getAmount());

        return savedTransaction;
    }

    @Override
    @Transactional
    public Transaction transfer(TransactionDTO request, Account fromAccount, Account toAccount) {
        // Check sufficient balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(request.getDescription());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);

        // Update account balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Update transaction status
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the action
        auditLogService.logAction(fromAccount, AuditLog.AuditAction.TRANSFER, 
            fromAccount.getAccountNumber(), "Transfer to " + toAccount.getAccountNumber() + ": " + request.getAmount());

        return savedTransaction;
    }

    @Override
    public List<Transaction> getAccountTransactions(Account account) {
        return transactionRepository.findByAccountOrderByTimestampDesc(account);
    }

    @Override
    public Transaction getTransaction(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
} 