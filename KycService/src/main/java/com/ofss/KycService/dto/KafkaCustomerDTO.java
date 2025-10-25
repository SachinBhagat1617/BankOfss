package com.ofss.KycService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KafkaCustomerDTO {
    private CustomerData customerData;
    private String VerificationStatus;
}
