package com.ofss.Account.service;

import com.ofss.Account.dto.CustomerData;
import com.ofss.Account.dto.CustomerResponseDTO;
import com.ofss.Account.dto.ResponseDTO;
import com.ofss.Account.exception.APIException;
import com.ofss.Account.exception.ResourceNotFoundException;
import com.ofss.Account.model.Account;
import com.ofss.Account.model.AccountStatus;
import com.ofss.Account.model.AccountType;
import com.ofss.Account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final WebClient webClient;

    @Override
    public ResponseEntity<ResponseDTO> updateAccountType(Long id, String type) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new APIException("Account not found", HttpStatus.NOT_FOUND));
        if(!isValidAccountType(type)) {
            throw new APIException("Invalid account type", HttpStatus.BAD_REQUEST);
        }
        account.setAccountType(AccountType.valueOf(type.toUpperCase()));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        ResponseDTO responseDTO =ResponseDTO.builder()
                .message("Account type updated successfully")
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .data(null)
                .build();
        return ResponseEntity.ok(responseDTO);
    }
    @Override
    public ResponseEntity<ResponseDTO> getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "ID", id.toString()));

        ResponseDTO response = ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Account retrieved successfully")
                .data(account)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ResponseDTO> getAllAccounts(Long userId) {
        if(getCustomer(userId).getRole().equalsIgnoreCase("USER")) {
            throw new APIException("Only admins can view all accounts", HttpStatus.FORBIDDEN);
        }

        List<Account> accounts = accountRepository.findAll();
        if (accounts.isEmpty()) {
            throw new APIException("No accounts yet  ", HttpStatus.NOT_FOUND);
        }

        ResponseDTO response = ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Accounts retrieved successfully")
                .data(accounts)
                .build();

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ResponseDTO> updateAccountStatus(Long accountId, String status, Long adminId) {
        if(getCustomer(adminId).getRole().equalsIgnoreCase("USER")) {
            throw new APIException("Only admins can update account status", HttpStatus.FORBIDDEN);
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "ID", accountId.toString()));
        if(!isValidAccountStatus(status)){
            throw new APIException("Invalid account status", HttpStatus.BAD_REQUEST);
        }
        account.setAccountStatus(AccountStatus.valueOf(status));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        ResponseDTO response = ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Account status updated successfully")
                .data(account)
                .build();

        return ResponseEntity.ok(response);
    }

    private boolean isValidAccountStatus(String status) {
        for (AccountStatus accountStatus : AccountStatus.values()) {
            if (accountStatus.name().equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResponseEntity<ResponseDTO> addBalance(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "ID", accountId.toString()));
        if(account.getAccountStatus()==AccountStatus.CLOSED){
            throw new APIException("Cannot credit balance from a closed account", HttpStatus.BAD_REQUEST);
        }
        if (account.getAccountStatus()==AccountStatus.INACTIVE){
            throw new APIException("Cannot credit balance from a INACTIVE account", HttpStatus.BAD_REQUEST);
        }
        if (amount <= 0) {
            throw new APIException("Amount must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        account.setBalance(account.getBalance().add(BigDecimal.valueOf(amount)));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        ResponseDTO response = ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Balance added successfully")
                .data(account)
                .build();
        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ResponseDTO> deductBalance(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "ID", accountId.toString()));
        if(account.getAccountStatus()==AccountStatus.CLOSED){
            throw new APIException("Cannot deduct balance from a closed account", HttpStatus.BAD_REQUEST);
        }
        if (account.getAccountStatus()==AccountStatus.INACTIVE){
            throw new APIException("Cannot deduct balance from a INACTIVE account", HttpStatus.BAD_REQUEST);
        }


        if (amount <= 0) {
            throw new APIException("Amount must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        if (account.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new APIException("Insufficient balance", HttpStatus.BAD_REQUEST);
        }

        account.setBalance(account.getBalance().subtract(BigDecimal.valueOf(amount)));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        ResponseDTO response = ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Balance deducted successfully")
                .data(account)
                .build();
        return ResponseEntity.ok(response);

    }

    @Override
    public ResponseEntity<ResponseDTO> getAccountsByCustomerId(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        if (accounts.isEmpty()) {
            throw new APIException("No accounts found for customer ID: " + customerId, HttpStatus.NOT_FOUND);
        }

        ResponseDTO response = ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Accounts retrieved successfully")
                .data(accounts)
                .build();

        return ResponseEntity.ok(response);
    }


    private boolean isValidAccountType(String type) {
        for (AccountType accountType : AccountType.values()) {
            if (accountType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private CustomerData getCustomer(Long id) {
        try {
            CustomerResponseDTO response = webClient.get()
                    .uri("/customers/id/{id}", id)
                    .retrieve()
                    .bodyToMono(CustomerResponseDTO.class)
                    .block();

            if (response == null || response.getData() == null) {
                throw new APIException("Customer not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return response.getData();

        } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Error fetching customer with ID: " + id + ". Error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
