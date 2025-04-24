package com.bankofmadras.repository;

import com.bankofmadras.model.Account;
import com.bankofmadras.model.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {
    List<FixedDeposit> findByAccountAndStatus(Account account, FixedDeposit.FixedDepositStatus status);
    List<FixedDeposit> findByMaturityDateBeforeAndStatus(LocalDateTime date, FixedDeposit.FixedDepositStatus status);
} 