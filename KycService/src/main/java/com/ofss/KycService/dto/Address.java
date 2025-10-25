package com.ofss.KycService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String city;
    private String street;
    private String country;
    private String state;
    private String pincode;
}