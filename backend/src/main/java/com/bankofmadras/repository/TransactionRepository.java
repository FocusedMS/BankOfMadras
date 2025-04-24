package com.bankofmadras.repository;

import com.bankofmadras.model.Account;
import com.bankofmadras.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByFromAccountOrToAccount(Account fromAccount, Account toAccount, Pageable pageable);
    List<Transaction> findByFromAccountAndTimestampBetween(Account account, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByToAccountAndTimestampBetween(Account account, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByAccountAccountNumberAndCreatedAtBetween(
            String accountNumber, LocalDateTime startDate, LocalDateTime endDate);
    Page<Transaction> findByAccountAccountNumber(String accountNumber, Pageable pageable);
    List<Transaction> findByAccount_AccountNumberOrderByTimestampDesc(String accountNumber);
    List<Transaction> findByAccount_AccountNumberAndTimestampBetween(
        String accountNumber, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByAccount(Account account);
    List<Transaction> findByAccountOrderByTimestampDesc(Account account);
} 