package com.ofss.Customer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponseDTO {
    private String city;
    private String street;
    private String country;
    private String state;
    private String pincode;
}