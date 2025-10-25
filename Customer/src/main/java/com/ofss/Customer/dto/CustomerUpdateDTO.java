package com.ofss.Customer.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String phone;
    private LocalDate dateOfBirth;
    private UpdateAdressDTO address;
}