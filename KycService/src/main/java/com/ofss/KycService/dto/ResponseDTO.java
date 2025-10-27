package com.ofss.KycService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDTO {
    private String message;
    private Integer statusCode;
    private Boolean success;
    private Object data;
}
