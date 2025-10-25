package com.ofss.Customer.Controller;


import com.ofss.Customer.dto.CustomerRequestDTO;
import com.ofss.Customer.dto.CustomerUpdateDTO;
import com.ofss.Customer.dto.ResponseDTO;
import com.ofss.Customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
@Tag(name="Customer Management", description="APIs for managing customers")
public class CustomerController {
    private final CustomerService customerService;
    @GetMapping
    public ResponseEntity<ResponseDTO> getAllCustomers() {
        return customerService.getAllCustomers();
    }
    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createCustomer(@RequestBody @Valid CustomerRequestDTO customerRequestDTO) {
        return customerService.createCustomer(customerRequestDTO);
    }
    @PutMapping("/update/id/{id}")
    public ResponseEntity<ResponseDTO> updateCustomer(@RequestBody @Valid CustomerUpdateDTO customerRequestDTO, @PathVariable Long id) {
        return customerService.updateCustomer(customerRequestDTO,id);
    }
    @DeleteMapping("/id/{id}")
    public ResponseEntity<ResponseDTO> deleteCustomer(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
    @GetMapping("/id/{id}")
    public ResponseEntity<ResponseDTO> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }
    @PutMapping("/id/{userId}")
    public ResponseEntity<ResponseDTO> updateRole(@PathVariable Long userId,
                                                  @RequestParam String role,
                                                  @RequestHeader("X-User-Id") Long adminId) {
        return customerService.updateCustomerRole(userId,role,adminId);
    }

}
