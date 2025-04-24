package com.bankofmadras.service;

import com.bankofmadras.dto.AccountRegistrationRequest;
import com.bankofmadras.dto.AccountResponse;
import com.bankofmadras.model.Account;

import java.util.List;

public interface AccountService {
    AccountResponse registerAccount(AccountRegistrationRequest request);
    AccountResponse getAccountByEmail(String email);
    AccountResponse getAccountByAccountNumber(String accountNumber);
    List<AccountResponse> getAllAccounts();
    AccountResponse updateAccount(String accountNumber, AccountRegistrationRequest request);
    void deleteAccount(String accountNumber);
    void blockAccount(String accountNumber);
    void unblockAccount(String accountNumber);
    Account getAccountEntityByEmail(String email);
    Account getAccountEntityByAccountNumber(String accountNumber);
} 