package com.ofss.Account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        description = "Standard API response wrapper containing status, message, and data",
        example = """
        {
          "success": true,
          "statusCode": 200,
          "message": "Operation completed successfully",
          "data": {
            "accountId": 1,
            "accountNumber": "ACC123456",
            "balance": 5000.00
          }
        }
        """
)
public class ResponseDTO {

    @Schema(
            description = "Indicates whether the operation was successful",
            example = "true",
            required = true
    )
    private Boolean success;

    @Schema(
            description = "HTTP status code of the response",
            example = "200",
            required = true,
            allowableValues = {"200", "201", "400", "401", "403", "404", "500"}
    )
    private Integer statusCode;

    @Schema(
            description = "Human-readable message describing the result of the operation",
            example = "Account retrieved successfully",
            required = true
    )
    private String message;

    @Schema(
            description = "Response payload containing the actual data. Can be a single object, list, or null",
            example = "{\"accountId\": 1, \"balance\": 5000.00}",
            nullable = true
    )
    private Object data;
}