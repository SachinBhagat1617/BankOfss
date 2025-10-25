package com.ofss.KycService.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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