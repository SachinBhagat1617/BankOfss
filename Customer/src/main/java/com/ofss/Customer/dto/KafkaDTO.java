package com.ofss.Customer.dto;

import com.ofss.Customer.model.Customer;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class KafkaDTO {
    private CustomerData customerData;
    private String message;
}
