package com.bankofmadras.service;

import com.bankofmadras.dto.FixedDepositDTO;
import com.bankofmadras.dto.TransactionDTO;
import com.bankofmadras.model.Account;
import com.bankofmadras.model.AuditLog;
import com.bankofmadras.model.FixedDeposit;
import com.bankofmadras.model.Transaction;
import com.bankofmadras.repository.AccountRepository;
import com.bankofmadras.repository.FixedDepositRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FixedDepositService {
    private final AccountRepository accountRepository;
    private final FixedDepositRepository fixedDepositRepository;
    private final TransactionService transactionService;
    private final AuditLogService auditLogService;

    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.05"); // 5% annual interest rate

    @Transactional
    public FixedDeposit createFixedDeposit(FixedDepositDTO request, Account account) {
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance for fixed deposit");
        }

        // Calculate maturity amount
        BigDecimal maturityAmount = calculateMaturityAmount(request.getAmount(), request.getDurationMonths());

        // Create fixed deposit
        FixedDeposit fixedDeposit = new FixedDeposit();
        fixedDeposit.setAccount(account);
        fixedDeposit.setAmount(request.getAmount());
        fixedDeposit.setMaturityAmount(maturityAmount);
        fixedDeposit.setDurationMonths(request.getDurationMonths());
        fixedDeposit.setStartDate(LocalDateTime.now());
        fixedDeposit.setMaturityDate(LocalDateTime.now().plusMonths(request.getDurationMonths()));
        fixedDeposit.setStatus(FixedDeposit.FixedDepositStatus.ACTIVE);
        fixedDeposit.setDescription(request.getDescription());

        // Deduct amount from account
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        // Create transaction record
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(request.getAmount());
        transactionDTO.setDescription("Fixed deposit creation: " + request.getDescription());
        transactionService.withdraw(transactionDTO, account);

        FixedDeposit savedFD = fixedDepositRepository.save(fixedDeposit);

        // Log the action
        auditLogService.logAction(account, AuditLog.AuditAction.CREATED_FD, 
            account.getAccountNumber(), "Fixed deposit created for " + request.getAmount());

        return savedFD;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void processMaturedFixedDeposits() {
        List<FixedDeposit> maturedFDs = fixedDepositRepository.findByMaturityDateBeforeAndStatus(
            LocalDateTime.now(), FixedDeposit.FixedDepositStatus.ACTIVE);

        for (FixedDeposit fd : maturedFDs) {
            // Credit maturity amount to account
            Account account = fd.getAccount();
            account.setBalance(account.getBalance().add(fd.getMaturityAmount()));
            accountRepository.save(account);

            // Create transaction record
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setAmount(fd.getMaturityAmount());
            transactionDTO.setDescription("Fixed deposit maturity: " + fd.getDescription());
            transactionService.deposit(transactionDTO, account);

            // Update FD status
            fd.setStatus(FixedDeposit.FixedDepositStatus.MATURED);
            fixedDepositRepository.save(fd);

            // Log the action
            auditLogService.logAction(account, AuditLog.AuditAction.FD_MATURED, 
                account.getAccountNumber(), "Fixed deposit matured: " + fd.getMaturityAmount());
        }
    }

    private BigDecimal calculateMaturityAmount(BigDecimal principal, int months) {
        // Simple interest calculation: P(1 + rt)
        // where P = principal, r = annual interest rate, t = time in years
        BigDecimal timeInYears = new BigDecimal(months).divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
        return principal.multiply(BigDecimal.ONE.add(INTEREST_RATE.multiply(timeInYears)))
            .setScale(2, RoundingMode.HALF_UP);
    }
} 