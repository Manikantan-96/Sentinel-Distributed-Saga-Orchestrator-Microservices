package com.sentinelorchestratorservice.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowResponse {
    private Long workflowId;
    private String status;
    private String currentStep;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastError;
}
