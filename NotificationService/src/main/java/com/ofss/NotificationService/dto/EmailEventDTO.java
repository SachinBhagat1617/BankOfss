package com.ofss.NotificationService.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEventDTO {
    private String to;
    private String subject;
    private String body;
    private String templateType;
    private String customerName;

    private String accountNumber;
    private String accountType;
    private Double accountBalance;

    private String rejectionReason;
}
