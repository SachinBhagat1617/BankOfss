package com.ofss.Customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Valid
@Data
public class RoleDTO {
    @NotBlank(message = "Role name is required")
    private String roleName;
    @Email
    @NotBlank(message = "Email is required to change the role")
    private String email;
}
