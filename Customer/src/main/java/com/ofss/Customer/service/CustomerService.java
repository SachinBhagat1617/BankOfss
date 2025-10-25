package com.ofss.Customer.service;

import com.ofss.Customer.dto.CustomerRequestDTO;
import com.ofss.Customer.dto.CustomerUpdateDTO;
import com.ofss.Customer.dto.ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

public interface CustomerService {
    ResponseEntity<ResponseDTO> createCustomer(@Valid CustomerRequestDTO customerRequestDTO);

    ResponseEntity<ResponseDTO> updateCustomer(@Valid CustomerUpdateDTO customerRequestDTO, Long id);

    ResponseEntity<ResponseDTO> deleteCustomer(Long id);

    ResponseEntity<ResponseDTO> getCustomerById(Long id);

    ResponseEntity<ResponseDTO> getAllCustomers();

    ResponseEntity<ResponseDTO> updateCustomerRole(Long userId, String role, Long adminId);
}
