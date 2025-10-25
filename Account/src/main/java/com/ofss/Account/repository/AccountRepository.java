package com.ofss.Account.repository;

import com.ofss.Account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);
}
