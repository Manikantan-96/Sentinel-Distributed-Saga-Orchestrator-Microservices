package com.sentinelorchestratorservice.Entity;

import com.sentinelorchestratorservice.Enum.StepStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="workflow_steps")
public class WorkFlowStep {
    @Id
    @Column(name="step_id",nullable = false,updatable = false)
    private String stepId;

    @Column(name="workflow_id")
    private long workFlowId;

    @Column(name="step_order")
    private int stepOrder;
    @Column(name="step_name")
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name="step_status")
    private StepStatus status;

    @Lob
    @Column(name="input_payload")
    private String requestPayload;
    @Lob
    @Column(name="output_payload")
    private String responsePayload;

    @Lob
    @Column(name="error_details")
    private String errorMessage;

    @Column(name="started_at")
    private LocalDateTime startedAt;

    @Column(name="finished_at")
    private LocalDateTime finishedAt;
    @PrePersist
    public void onCreate(){
        LocalDateTime now=LocalDateTime.now();
        this.startedAt=now;
        this.finishedAt=now;
    }
    @PreUpdate
    public void onUpdate(){
        this.finishedAt=LocalDateTime.now();
    }

}
