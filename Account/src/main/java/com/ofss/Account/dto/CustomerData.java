package com.ofss.Account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String CustomerId;
    private String dateOfBirth;
    private Address address;
}