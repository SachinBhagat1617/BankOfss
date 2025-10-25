package com.ofss.KycService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerData {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String dateOfBirth;
    private String CustomerId;
    private Address address;
}