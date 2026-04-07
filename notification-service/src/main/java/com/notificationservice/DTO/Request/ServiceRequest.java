package com.notificationservice.DTO.Request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRequest {
    @NotNull
    private Long workflowId;
    @NotNull
    private Long userId;
    @NotNull
    private Long productId;
    @NotBlank
    private String workFlowStepId;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotNull
    private LocalDateTime calledTimer;
}
