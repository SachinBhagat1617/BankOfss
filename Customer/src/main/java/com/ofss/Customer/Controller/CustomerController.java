package com.ofss.Customer.Controller;


import com.ofss.Customer.dto.CustomerRequestDTO;
import com.ofss.Customer.dto.CustomerUpdateDTO;
import com.ofss.Customer.dto.LogInRequest;
import com.ofss.Customer.dto.ResponseDTO;
import com.ofss.Customer.service.CustomerService;
import com.ofss.Customer.service.KeyCloakAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name="Customer Management", description="APIs for managing customers")
public class CustomerController {
    private final CustomerService customerService;
    private final KeyCloakAdminService keyCloakAdminService;
    @GetMapping("/admin/customers")
    public ResponseEntity<ResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }
    @GetMapping("/public/customers/email/{email}")
    public ResponseEntity<ResponseDTO> getCustomerByEmail(@PathVariable String email) {
        return customerService.getCustomerByEmail(email);
    }
    @PostMapping("/auth/customers/create")
    public ResponseEntity<ResponseDTO> createCustomer(@RequestBody @Valid CustomerRequestDTO customerRequestDTO) {
        return customerService.createCustomer(customerRequestDTO);
    }
    @PutMapping("/public/customers/update/id/{id}")
    public ResponseEntity<ResponseDTO> updateCustomer(@RequestBody @Valid CustomerUpdateDTO customerRequestDTO, @PathVariable Long id) {
        return customerService.updateCustomer(customerRequestDTO,id);
    }
    @DeleteMapping("/public/customers/id/{id}")
    public ResponseEntity<ResponseDTO> deleteCustomer(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
    @GetMapping("/public/customers/id/{id}")
    public ResponseEntity<ResponseDTO> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }
    @PutMapping("/admin/customers/id/{userId}")
    public ResponseEntity<ResponseDTO> updateRole(@PathVariable Long userId,
                                                  @RequestParam String role,
                                                  @RequestHeader("X-User-Id") Long adminId) {
        return customerService.updateCustomerRole(userId,role,adminId);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody LogInRequest logInRequest, HttpServletResponse response) {
        keyCloakAdminService.login(logInRequest.getEmail(), logInRequest.getPassword(), response);
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        keyCloakAdminService.logout(response);
        return ResponseEntity.ok("Logout successful");
    }

}
