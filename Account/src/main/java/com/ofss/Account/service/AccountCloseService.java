package com.ofss.Account.service;

import com.ofss.Account.dto.CustomerData;
import com.ofss.Account.dto.CustomerResponseDTO;
import com.ofss.Account.dto.EmailEventDTO;
import com.ofss.Account.dto.KafkaCustomerDTO;
import com.ofss.Account.exception.APIException;
import com.ofss.Account.model.Account;
import com.ofss.Account.model.AccountStatus;
import com.ofss.Account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountCloseService {
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, EmailEventDTO> emailEventDTOKafkaTemplate;
    private final WebClient webClient;

    @KafkaListener(topics = "AccountCloseTopic", groupId = "account-service")
    public void closeAccount(KafkaCustomerDTO kafkaCustomerDTO) {
        System.out.println("Account closure process started for customer: " + kafkaCustomerDTO);
        Long customerId = Long.valueOf(kafkaCustomerDTO.getCustomerData().getCustomerId());
        CustomerData customer = kafkaCustomerDTO.getCustomerData();
        List<Account> accounts= accountRepository.findByCustomerId(customerId);
        accounts.stream().forEach(acc->{
            acc.setAccountStatus(AccountStatus.CLOSED);
            acc.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(acc);
            EmailEventDTO closureMail = EmailEventDTO.builder()
                    .to(customer.getEmail())
                    .subject("Account Closure Confirmation")
                    .body("Dear " + customer.getFirstName() + ",\n\n" +
                            "We confirm that your account with Account Number: " + acc.getAccountNumber() + " has been successfully closed.\n\n" +
                            "Thank you for banking with us.")
                    .templateType("ACCOUNT_CLOSURE")
                    .customerName(customer.getFirstName())
                    .accountNumber(acc.getAccountNumber())
                    .build();
            emailEventDTOKafkaTemplate.send("EmailNotificationTopic", closureMail);
        });
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
