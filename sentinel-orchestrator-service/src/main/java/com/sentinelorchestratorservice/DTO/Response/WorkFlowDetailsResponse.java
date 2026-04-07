package com.sentinelorchestratorservice.DTO.Response;
import com.sentinelorchestratorservice.Entity.WorkFlowStep;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowDetailsResponse {
    private Long workFlowId;
    private String workFlowType;
    private String status;
    private String currentStep;
    private String payload;
    private String lastError;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<WorkFlowStep> steps;
}
