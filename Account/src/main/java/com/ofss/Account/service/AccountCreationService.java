package com.ofss.Account.service;

import com.ofss.Account.dto.CustomerData;
import com.ofss.Account.dto.EmailEventDTO;
import com.ofss.Account.dto.KafkaCustomerDTO;
import com.ofss.Account.exception.APIException;
import com.ofss.Account.model.Account;
import com.ofss.Account.model.AccountStatus;
import com.ofss.Account.model.AccountType;
import com.ofss.Account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountCreationService {
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, EmailEventDTO> emailEventDTOKafkaTemplate;
    private final String EmailNotificationTopic = "EmailNotificationTopic";
    @KafkaListener(topics = "CustomerKYCTopic", groupId = "account-service")
    public void createAccount(KafkaCustomerDTO kafkaCustomerDTO){
        System.out.println("Account creation process started for customer: " + kafkaCustomerDTO);
        if ("VERIFIED".equalsIgnoreCase(kafkaCustomerDTO.getVerificationStatus())) {
            CustomerData customer = kafkaCustomerDTO.getCustomerData();
            Account account= Account.builder()
                    .accountNumber(generateAccountNumber())
                    .customerId(Long.valueOf(customer.getCustomerId()))
                    .accountType(AccountType.SAVINGS)
                    .balance(BigDecimal.valueOf(0.0))
                    .accountStatus(AccountStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            accountRepository.save(account);
            EmailEventDTO approvedMail = EmailEventDTO.builder()
                    .to(customer.getEmail())
                    .subject("🎉 Your Account Has Been Approved!")
                    .body("Dear " + customer.getFirstName() + ",\n\n" +
                            "Your account has been successfully created.\n" +
                            "Account Number: " + account.getAccountNumber() + "\n" +
                            "Type: " + account.getAccountType() + "\n" +
                            "Balance: ₹" + account.getBalance() + "\n\n" +
                            "Thank you for choosing our bank!")
                    .templateType("ACCOUNT_APPROVED")
                    .customerName(customer.getFirstName())
                    .accountNumber(account.getAccountNumber())
                    .accountType(String.valueOf(account.getAccountType()))
                    .accountBalance(account.getBalance().doubleValue())
                    .build();
            emailEventDTOKafkaTemplate.send("EmailNotificationTopic", approvedMail);
        }else{
            throw new RuntimeException("KYC verification failed for customer: " + kafkaCustomerDTO);
        }
    }

    private String generateAccountNumber() {
        String accountNumber;
        int attempts = 0;
        int maxAttempts = 10;
        do {
            // Generate account number
            long timestamp = System.currentTimeMillis() % 1000000000000L; // 12 digits
            int random = ThreadLocalRandom.current().nextInt(1000, 10000); // 4 digits
            accountNumber = String.format("%012d%04d", timestamp, random);
            attempts++;
            if (attempts >= maxAttempts) {
                throw new APIException("Unable to generate unique account number", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

}
