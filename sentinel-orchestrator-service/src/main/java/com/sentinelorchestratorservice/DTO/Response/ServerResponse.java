package com.sentinelorchestratorservice.DTO.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {
    private String transactionId;
    private Double amount;
    private String status;
    private String errorMessage;
    private LocalDateTime respondedTime;

}
