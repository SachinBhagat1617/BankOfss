package com.ofss.Account.service;

import com.ofss.Account.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    ResponseEntity<ResponseDTO> updateAccountType(Long id, String type);

    ResponseEntity<ResponseDTO> getAccountById(Long id);

    ResponseEntity<ResponseDTO> getAllAccounts(Long userId);

    ResponseEntity<ResponseDTO> updateAccountStatus(Long accountId, String status, Long adminId);

    ResponseEntity<ResponseDTO> addBalance(Long accountId, Double amount);

    ResponseEntity<ResponseDTO> deductBalance(Long accountId, Double amount);

    ResponseEntity<ResponseDTO> getAccountsByCustomerId(Long customerId);
}
