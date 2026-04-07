package com.sentinelorchestratorservice.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {
    private Long workflowId;
    private Long userId;
    private Long productId;
    private String workFlowStepId;
    private int quantity;
    private LocalDateTime calledTimer;
}
