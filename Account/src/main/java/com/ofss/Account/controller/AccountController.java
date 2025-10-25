package com.ofss.Account.controller;

import com.ofss.Account.dto.ResponseDTO;
import com.ofss.Account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Tag(name = "Account Management", description = "APIs for managing bank accounts including balance operations, status updates, and account type modifications")
public class AccountController {

    private final AccountService accountService;

    @PutMapping("/updateType/id/{id}/type/{type}")
    @Operation(
            summary = "Update account type",
            description = "Modifies the type of an existing bank account (e.g., SAVINGS, CURRENT, FIXED_DEPOSIT)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account type updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account type provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> updateAccountType(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "New account type (SAVINGS, CURRENT, FIXED_DEPOSIT)", required = true, example = "SAVINGS")
            @PathVariable String type
    ) {
        return accountService.updateAccountType(id, type);
    }

    @GetMapping("/id/{id}")
    @Operation(
            summary = "Get account by ID",
            description = "Retrieves detailed information about a specific account using its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> getAccountById(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long id
    ) {
        return accountService.getAccountById(id);
    }

    @PutMapping("/id/{accountId}/status/{status}")
    @Operation(
            summary = "Update account status",
            description = "Updates the status of an account (ACTIVE, INACTIVE, FROZEN, CLOSED). Requires admin privileges."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account status updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized - Admin access required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status provided",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> updateAccountStatus(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long accountId,
            @Parameter(description = "New account status (ACTIVE, INACTIVE, FROZEN, CLOSED)", required = true, example = "ACTIVE")
            @PathVariable String status,
            @Parameter(description = "Admin user ID performing the operation", required = true, example = "100")
            @RequestHeader("X-User-Id") Long adminId
    ) {
        return accountService.updateAccountStatus(accountId, status, adminId);
    }

    @PutMapping("/id/{accountId}/add/{amount}")
    @Operation(
            summary = "Add balance to account",
            description = "Credits the specified amount to the account balance"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance added successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid amount (must be positive)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> addBalance(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long accountId,
            @Parameter(description = "Amount to add (must be positive)", required = true, example = "1000.00")
            @PathVariable Double amount
    ) {
        return accountService.addBalance(accountId, amount);
    }

    @PutMapping("/id/{accountId}/deduct/{amount}")
    @Operation(
            summary = "Deduct balance from account",
            description = "Debits the specified amount from the account balance if sufficient funds are available"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance deducted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid amount or insufficient balance",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> deductBalance(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long accountId,
            @Parameter(description = "Amount to deduct (must be positive)", required = true, example = "500.00")
            @PathVariable Double amount
    ) {
        return accountService.deductBalance(accountId, amount);
    }

    @GetMapping("/customerId/{customerId}")
    @Operation(
            summary = "Get accounts by customer ID",
            description = "Retrieves all accounts associated with a specific customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No accounts found for the given customer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> getAccountsByCustomerId(
            @Parameter(description = "Customer ID", required = true, example = "50")
            @PathVariable Long customerId
    ) {
        return accountService.getAccountsByCustomerId(customerId);
    }

    @GetMapping
    @Operation(
            summary = "Get all accounts",
            description = "Retrieves a list of all accounts in the system. Requires user authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - User ID required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class)
                    )
            )
    })
    public ResponseEntity<ResponseDTO> getAllAccounts(
            @Parameter(description = "Authenticated user ID", required = true, example = "100")
            @RequestHeader("X-User-Id") Long userId
    ) {
        return accountService.getAllAccounts(userId);
    }
}