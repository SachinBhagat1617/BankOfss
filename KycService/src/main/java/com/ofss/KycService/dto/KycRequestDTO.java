package com.ofss.KycService.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class KycRequestDTO {

    @NotNull(message = "Customer ID is required")
    private Long customerId;
    private MultipartFile panFile;
    private MultipartFile aadhaarFile;
    private MultipartFile photoFile;

    private String status;
    private String remarks;
}
