package com.sentinelorchestratorservice.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowRequest {
    @NotBlank
    private String workflowType;
    @NotNull
    private Long userId;
    @NotNull
    private Long productId;
    @NotNull
    private Integer quantity;
    private String message;
}
