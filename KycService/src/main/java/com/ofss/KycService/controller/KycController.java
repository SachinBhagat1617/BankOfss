package com.ofss.KycService.controller;

import com.ofss.KycService.dto.KycRequestDTO;
import com.ofss.KycService.dto.ResponseDTO;
import com.ofss.KycService.service.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
@Tag(
        name = "KYC Management",
        description = "APIs for managing Know Your Customer (KYC) documents including upload, update, verification, and deletion of customer identity documents"
)
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/upload/id/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload KYC documents",
            description = """
            Uploads KYC documents for a customer including PAN card, Aadhaar card, and photograph.
            All three documents are mandatory for successful KYC submission.
            
            **Accepted File Formats:** JPG, JPEG, PNG, PDF
            **Maximum File Size:** 10MB per file
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "KYC documents uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": true,
                          "statusCode": 200,
                          "message": "KYC documents uploaded successfully. Verification pending.",
                          "data": {
                            "kycId": 1,
                            "customerId": 50,
                            "status": "PENDING",
                            "submittedDate": "2025-10-25T15:30:00"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format or missing required documents",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": false,
                          "statusCode": 400,
                          "message": "PAN file is required",
                          "data": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "KYC already exists for this customer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> uploadKyc(
            @Parameter(
                    description = "Customer ID for whom KYC documents are being uploaded",
                    required = true,
                    example = "50"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "PAN Card document (JPG, PNG, PDF - Max 10MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("panFile")
            @NotNull(message = "PAN file is required")
            MultipartFile panFile,

            @Parameter(
                    description = "Aadhaar Card document (JPG, PNG, PDF - Max 10MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("aadhaarFile")
            @NotNull(message = "Aadhaar file is required")
            MultipartFile aadhaarFile,

            @Parameter(
                    description = "Customer photograph (JPG, PNG - Max 10MB)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("photoFile")
            @NotNull(message = "Photo file is required")
            MultipartFile photoFile
    ) {
        KycRequestDTO requestDTO = KycRequestDTO.builder()
                .customerId(id)
                .panFile(panFile)
                .aadhaarFile(aadhaarFile)
                .photoFile(photoFile)
                .build();
        System.out.println("Received KYC upload request for Customer ID: " + id);
        return kycService.uploadKyc(requestDTO, id);
    }

    @PutMapping(value = "/update/id/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update KYC documents",
            description = """
            Updates existing KYC documents for a customer. All file parameters are optional - 
            only provide the documents that need to be updated.
            
            **Use Case:** When customer needs to replace one or more documents due to rejection or updates.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "KYC documents updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": true,
                          "statusCode": 200,
                          "message": "KYC documents updated successfully",
                          "data": {
                            "kycId": 1,
                            "customerId": 50,
                            "status": "PENDING",
                            "updatedDate": "2025-10-25T16:00:00"
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "KYC record not found for the given customer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid file format or file size exceeded",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> updateKycStatus(
            @Parameter(
                    description = "Customer ID whose KYC documents need to be updated",
                    required = true,
                    example = "50"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Updated PAN Card document (optional)",
                    required = false,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "panFile", required = false)
            MultipartFile panFile,

            @Parameter(
                    description = "Updated Aadhaar Card document (optional)",
                    required = false,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "aadhaarFile", required = false)
            MultipartFile aadhaarFile,

            @Parameter(
                    description = "Updated customer photograph (optional)",
                    required = false,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "photoFile", required = false)
            MultipartFile photoFile
    ) {
        KycRequestDTO requestDTO = KycRequestDTO.builder()
                .customerId(id)
                .panFile(panFile)
                .aadhaarFile(aadhaarFile)
                .photoFile(photoFile)
                .build();
        System.out.println("Received KYC update request for Customer ID: " + id);
        return kycService.updateKYC(requestDTO, id);
    }

    @PutMapping("/view/{customerId}")
    @Operation(
            summary = "View and verify KYC documents",
            description = """
            Returns an HTML page displaying all KYC documents for verification purposes.
            Verifiers can approve or reject the KYC by providing a status parameter.
            
            **Status Options:** APPROVED, REJECTED, PENDING
            **Authorization:** Requires Verifier-ID header
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "KYC documents retrieved and displayed successfully",
                    content = @Content(
                            mediaType = "text/html",
                            schema = @Schema(type = "string", format = "html"),
                            examples = @ExampleObject(
                                    value = "<html><body><h1>KYC Documents for Customer ID: 50</h1>...</body></html>"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "KYC documents not found for the given customer",
                    content = @Content(mediaType = "text/html")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized - Invalid or missing Verifier-ID",
                    content = @Content(mediaType = "text/html")
            )
    })
    public ResponseEntity<String> viewAllImagesAndUpdateStatus(
            @Parameter(
                    description = "Customer ID whose KYC documents need to be viewed",
                    required = true,
                    example = "50"
            )
            @PathVariable Long customerId,

            @Parameter(
                    description = "KYC verification status (APPROVED, REJECTED, PENDING)",
                    required = false,
                    example = "APPROVED",
                    schema = @Schema(allowableValues = {"APPROVED", "REJECTED", "PENDING"})
            )
            @RequestParam(required = false) String status,

            @Parameter(
                    description = "ID of the verifier performing the KYC verification",
                    required = true,
                    example = "100"
            )
            @RequestHeader("Verifier-ID") Long verifierId
    ) {
        System.out.println("Verifier ID: " + verifierId);
        String htmlResponse = kycService.viewAllImagesAndUpdateStatus(customerId, status, verifierId);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(htmlResponse);
    }

    @DeleteMapping("/delete/id/{id}")
    @Operation(
            summary = "Delete KYC records",
            description = """
            Permanently deletes KYC documents and records for a specific customer.
            This action requires verifier authorization and cannot be undone.
            
            **Use Case:** Remove KYC data for account closure or data cleanup.
            **Authorization:** Requires Verifier-ID header
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "KYC records deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "success": true,
                          "statusCode": 200,
                          "message": "KYC records deleted successfully",
                          "data": null
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "KYC record not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized - Invalid or missing Verifier-ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> deleteKyc(
            @Parameter(
                    description = "Customer ID whose KYC records need to be deleted",
                    required = true,
                    example = "50"
            )
            @PathVariable Long id,

            @Parameter(
                    description = "ID of the verifier authorizing the deletion",
                    required = true,
                    example = "100"
            )
            @RequestHeader("Verifier-ID") Long verifierId
    ) {
        String responseMessage = kycService.deleteKyc(id, verifierId);
        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(200)
                .message(responseMessage)
                .data(null)
                .build());
    }
    @GetMapping("/status/id/{id}")
    @Operation(
            summary = "View KYC status",
            description = "Allows a user to view the current status of their KYC verification."
    )
    public ResponseEntity<ResponseDTO> userViewKycStatus(@PathVariable Long id) {
        return kycService.userViewKycStatus(id);
    }

    @GetMapping
    @Operation(
            summary = "Get all KYC records",
            description = "Allows an admin to retrieve all KYC records in the system."
    )
    public ResponseEntity<ResponseDTO> getAllKycRecords(@RequestHeader ("X-User-Id") Long adminId) {
        return kycService.getAllKycRecords(adminId);
    }

}