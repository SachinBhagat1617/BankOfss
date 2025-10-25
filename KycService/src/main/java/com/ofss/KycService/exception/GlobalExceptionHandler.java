package com.ofss.KycService.exception;

import com.ofss.KycService.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // global exception handler for REST controllers and returns JSON responses
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        ResponseDTO response = com.ofss.KycService.dto.ResponseDTO.builder()
                .success(false)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ResponseDTO> handleAPIException(APIException ex) {
        ResponseDTO response = ResponseDTO.builder()
                .success(false)
                .statusCode(ex.getStatus().value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(ex.getStatus().value()).body(response);
    }

    // Handle all other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO> handleGenericException(Exception ex) {
        ResponseDTO response = ResponseDTO.builder()
                .success(false)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred: " + ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
