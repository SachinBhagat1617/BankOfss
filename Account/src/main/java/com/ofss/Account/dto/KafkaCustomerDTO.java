package com.ofss.Account.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaCustomerDTO {
    private CustomerData customerData;
    private String verificationStatus;
}
