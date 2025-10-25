package com.ofss.Account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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