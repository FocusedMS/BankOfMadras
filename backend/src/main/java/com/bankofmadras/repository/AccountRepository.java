package com.bankofmadras.repository;

import com.bankofmadras.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
} 