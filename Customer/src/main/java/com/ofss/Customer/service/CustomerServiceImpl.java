package com.ofss.Customer.service;

import com.ofss.Customer.dto.*;
import com.ofss.Customer.exception.APIException;
import com.ofss.Customer.exception.ResourceNotFoundException;
import com.ofss.Customer.model.Address;
import com.ofss.Customer.model.Customer;
import com.ofss.Customer.model.Role;
import com.ofss.Customer.repository.AddressRepository;
import com.ofss.Customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final KeyCloakAdminService keyCloakAdminService;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final KafkaTemplate<String,KafkaDTO> closeAccountKafkaTemplate;
    private final String AccountCloseTopic="AccountCloseTopic";

    @Override
    public ResponseEntity<ResponseDTO> createCustomer(CustomerRequestDTO customerRequestDTO) {

        if (customerRequestDTO.getEmail() == null ||
                customerRequestDTO.getFirstName() == null ||
                customerRequestDTO.getLastName() == null ||
                customerRequestDTO.getUsername() == null ||
                customerRequestDTO.getAadhaarNumber() == null ||
                customerRequestDTO.getPanNumber() == null) {
            throw new APIException("Missing required fields for creating customer", HttpStatus.BAD_REQUEST);
        }
        String token= keyCloakAdminService.getAdminAccessToken();
        String keyCloakId= keyCloakAdminService.createUser(token,customerRequestDTO);
        if(keyCloakId==null){
            throw new APIException("Error creating user in Keycloak", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        keyCloakAdminService.assignClientRoleToUser(customerRequestDTO.getFirstName(),String.valueOf(Role.USER),keyCloakId);

        Customer existingCustomer = customerRepository.findByEmail(customerRequestDTO.getEmail());
        if(existingCustomer!=null && existingCustomer.isInActive()){
            customerRepository.delete(existingCustomer);
        }
        if (customerRepository.existsByEmail(customerRequestDTO.getEmail())) {
            throw new APIException("Email already exists", HttpStatus.BAD_REQUEST);
        }
        if (customerRepository.existsByUsername(customerRequestDTO.getUsername())) {
            throw new APIException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        if (customerRequestDTO.getAddress() != null) {
            AddressRequestDTO addr = customerRequestDTO.getAddress();
            if (addr.getCity() == null || addr.getState() == null || addr.getCountry() == null ||
                    addr.getPincode() == null || addr.getStreet() == null) {
                throw new APIException("Address must have city, state, country, pincode, and street", HttpStatus.BAD_REQUEST);
            }
        }
        // Build the customer object
        Customer customer = Customer.builder()
                .firstName(customerRequestDTO.getFirstName())
                .lastName(customerRequestDTO.getLastName())
                .role(Role.USER)
                .email(customerRequestDTO.getEmail())
                .phone(customerRequestDTO.getPhone())
                .username(customerRequestDTO.getUsername())
                .dateOfBirth(customerRequestDTO.getDateOfBirth())
                .aadhaarNumber(customerRequestDTO.getAadhaarNumber())
                .panNumber(customerRequestDTO.getPanNumber())
                .KeycloakId(keyCloakId)
                .inActive(false)
                .address(customerRequestDTO.getAddress() != null ?
                        Address.builder()
                                .city(customerRequestDTO.getAddress().getCity())
                                .state(customerRequestDTO.getAddress().getState())
                                .country(customerRequestDTO.getAddress().getCountry())
                                .pincode(customerRequestDTO.getAddress().getPincode())
                                .street(customerRequestDTO.getAddress().getStreet())
                                .build() : null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder()
                        .success(true)
                        .statusCode(HttpStatus.CREATED.value())
                        .message("Customer created successfully")
                        .data(getCustomerResponseDTO(savedCustomer))
                        .build());
    }

    @Override
    public ResponseEntity<ResponseDTO> updateCustomer(CustomerUpdateDTO customerRequestDTO, Long id) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new APIException("Customer not found with id: " + id, HttpStatus.NOT_FOUND));

        boolean updated = false;

        if (customerRequestDTO.getEmail() != null) {
            existingCustomer.setEmail(customerRequestDTO.getEmail());
            updated = true;
        }
        if (customerRequestDTO.getFirstName() != null) {
            existingCustomer.setFirstName(customerRequestDTO.getFirstName());
            updated = true;
        }
        if (customerRequestDTO.getLastName() != null) {
            existingCustomer.setLastName(customerRequestDTO.getLastName());
            updated = true;
        }
        if (customerRequestDTO.getPhone() != null) {
            existingCustomer.setPhone(customerRequestDTO.getPhone());
            updated = true;
        }
        if (customerRequestDTO.getUsername() != null) {
            existingCustomer.setUsername(customerRequestDTO.getUsername());
            updated = true;
        }
        if (customerRequestDTO.getDateOfBirth() != null) {
            existingCustomer.setDateOfBirth(customerRequestDTO.getDateOfBirth());
            updated = true;
        }
        if(customerRequestDTO.getAadhaarNumber()!=null){
            existingCustomer.setAadhaarNumber(customerRequestDTO.getAadhaarNumber());
            updated=true;
        }
        if(customerRequestDTO.getPanNumber()!=null){
            existingCustomer.setPanNumber(customerRequestDTO.getPanNumber());
            updated=true;
        }
        if (customerRequestDTO.getAddress() != null) {
            Address address = existingCustomer.getAddress();
            if (address == null) address = new Address();

            boolean addressUpdated = false;

            if (customerRequestDTO.getAddress().getCity() != null) {
                address.setCity(customerRequestDTO.getAddress().getCity());
                addressUpdated = true;
            }
            if (customerRequestDTO.getAddress().getState() != null) {
                address.setState(customerRequestDTO.getAddress().getState());
                addressUpdated = true;
            }
            if (customerRequestDTO.getAddress().getCountry() != null) {
                address.setCountry(customerRequestDTO.getAddress().getCountry());
                addressUpdated = true;
            }
            if (customerRequestDTO.getAddress().getPincode() != null) {
                address.setPincode(customerRequestDTO.getAddress().getPincode());
                addressUpdated = true;
            }
            if (customerRequestDTO.getAddress().getStreet() != null) {
                address.setStreet(customerRequestDTO.getAddress().getStreet());
                addressUpdated = true;
            }


            if (addressUpdated) {
                existingCustomer.setAddress(address);
                updated = true;
            }
        }
        // If nothing was updated, throw an error
        if (!updated) {
            throw new APIException("No valid fields found to update", HttpStatus.BAD_REQUEST);
        }else{
            existingCustomer.setUpdatedAt(LocalDateTime.now());
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Customer updated successfully")
                .data(getCustomerResponseDTO(updatedCustomer))
                .build());
    }


    @Transactional
    @Override
    public ResponseEntity<ResponseDTO> deleteCustomer(Long id) {
        // Fetch existing customer
        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isEmpty()) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        if(existingCustomerOpt.get().isInActive()){
            throw new APIException("Customer already deleted with id: " + id, HttpStatus.BAD_REQUEST);
        }

        Customer customer = existingCustomerOpt.get();

        // Soft delete the customer in DB
        customerRepository.softDeleteById(id);

        // Build KafkaCustomerDTO for account closure
        KafkaDTO kafkaCustomerDTO = KafkaDTO.builder()
                .customerData(
                        CustomerData.builder()
                                .customerId(String.valueOf(customer.getId()))
                                .firstName(customer.getFirstName())
                                .lastName(customer.getLastName())
                                .email(customer.getEmail())
                                .username(customer.getUsername())
                                .phone(customer.getPhone())
                                .build()
                )
                .message("CLOSED") // Optional field to indicate closure
                .build();

        // Send to AccountCloseTopic
        closeAccountKafkaTemplate.send(AccountCloseTopic, kafkaCustomerDTO);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .success(true)
                        .statusCode(HttpStatus.OK.value())
                        .message("Customer deleted successfully and account closure initiated")
                        .build()
        );
    }



    @Override
    public ResponseEntity<ResponseDTO> getCustomerById(Long id) {
        Optional<Customer> customerOpt = customerRepository.findById(id);

        if (customerOpt.isEmpty()) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        if(customerOpt.get().isInActive()){
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Customer retrieved successfully")
                .data(getCustomerResponseDTO(customerOpt.get()))
                .build());
    }

    @Override
    public ResponseEntity<ResponseDTO> getAllCustomers() {
//        Optional<Customer> adminCustomer = customerRepository.findById(adminId);
//        if (adminCustomer.isEmpty()) {
//            throw new ResourceNotFoundException("Customer", "id", adminId);
//        }
//        if(adminCustomer.get().getRole() != Role.ADMIN){
//            throw new APIException("Only ADMIN users can view all customers", HttpStatus.FORBIDDEN);
//        }
        List<Customer> customers = customerRepository.findAll();
        System.out.println(customers);

        if (customers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.builder()
                            .success(false)
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("No customers found")
                            .data(Collections.emptyList())
                            .build());
        }

        List<CustomerResponseDTO> customerList = customers.stream()
                .map(this::getCustomerResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Customers retrieved successfully")
                .data(customerList)
                .build());
    }


    @Override
    public ResponseEntity<ResponseDTO> updateCustomerRole(Long userId, String role, Long adminId) {
        Optional<Customer> adminCustomer = customerRepository.findById(adminId);
        if (adminCustomer.isEmpty()) {
            throw new ResourceNotFoundException("Customer", "id", adminId);
        }

        if (adminCustomer.get().getRole() != Role.ADMIN) {
            throw new APIException("Only ADMIN users can update roles", HttpStatus.FORBIDDEN);
        }

        Optional<Customer> customerOpt = customerRepository.findById(userId);
        if (customerOpt.isEmpty()) {
            throw new ResourceNotFoundException("Customer", "id", userId);
        }

        Customer customer = customerOpt.get();
        try {
            customer.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new APIException("Invalid role: " + role, HttpStatus.BAD_REQUEST);
        }
        Customer updatedCustomer = customerRepository.save(customer);

        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Customer role updated successfully")
                .data(getCustomerResponseDTO(updatedCustomer))
                .build());
    }

    @Override
    public ResponseEntity<ResponseDTO> getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new APIException("Customer not found with email: " + email, HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("Customer retrieved successfully")
                .data(getCustomerResponseDTO(customer))
                .build());
    }


    private CustomerResponseDTO getCustomerResponseDTO(Customer customer) {
        Address address = customer.getAddress();
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .role(String.valueOf(customer.getRole()))
                .dateOfBirth(customer.getDateOfBirth())
                .address(AddressResponseDTO.builder()
                        .street(address != null ? address.getStreet() : null)
                        .city(address != null ? address.getCity() : null)
                        .state(address != null ? address.getState() : null)
                        .pincode(address != null ? address.getPincode() : null)
                        .country(address != null ? address.getCountry() : null)
                        .build())
                .build();
    }
}
