package com.ofss.Customer.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ofss.Customer.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CustomerData {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String customerId;
    private String dateOfBirth;
    private Address address;
}