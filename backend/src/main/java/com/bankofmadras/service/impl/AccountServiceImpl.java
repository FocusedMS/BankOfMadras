package com.bankofmadras.service.impl;

import com.bankofmadras.dto.AccountRegistrationRequest;
import com.bankofmadras.dto.AccountResponse;
import com.bankofmadras.exception.ResourceNotFoundException;
import com.bankofmadras.model.Account;
import com.bankofmadras.repository.AccountRepository;
import com.bankofmadras.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AccountResponse registerAccount(AccountRegistrationRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (accountRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("Mobile number already exists");
        }

        Account account = new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setMobile(request.getMobile());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setAccountNumber(generateAccountNumber());

        return AccountResponse.fromEntity(accountRepository.save(account));
    }

    @Override
    public AccountResponse getAccountByEmail(String email) {
        return AccountResponse.fromEntity(getAccountEntityByEmail(email));
    }

    @Override
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        return AccountResponse.fromEntity(getAccountEntityByAccountNumber(accountNumber));
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(String accountNumber, AccountRegistrationRequest request) {
        Account account = getAccountEntityByAccountNumber(accountNumber);
        
        if (!account.getEmail().equals(request.getEmail()) && 
            accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!account.getMobile().equals(request.getMobile()) && 
            accountRepository.existsByMobile(request.getMobile())) {
            throw new IllegalArgumentException("Mobile number already exists");
        }

        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setMobile(request.getMobile());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return AccountResponse.fromEntity(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteAccount(String accountNumber) {
        Account account = getAccountEntityByAccountNumber(accountNumber);
        if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete account with non-zero balance");
        }
        account.setDeleted(true);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void blockAccount(String accountNumber) {
        Account account = getAccountEntityByAccountNumber(accountNumber);
        account.setActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void unblockAccount(String accountNumber) {
        Account account = getAccountEntityByAccountNumber(accountNumber);
        account.setActive(true);
        accountRepository.save(account);
    }

    @Override
    public Account getAccountEntityByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with email: " + email));
    }

    @Override
    public Account getAccountEntityByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + accountNumber));
    }

    private String generateAccountNumber() {
        // Generate a unique 7-digit number
        String randomDigits = String.format("%07d", new Random().nextInt(10000000));
        // Combine with bank code
        return "BOM" + randomDigits;
    }
} 