package com.notificationservice.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendingDetails {
    private Long workflowId;
    private String userName;
    private String userEmail;
    private String productName;
    private Integer quantity;
    private Double amount;
    private LocalDateTime calledTimer;
}
