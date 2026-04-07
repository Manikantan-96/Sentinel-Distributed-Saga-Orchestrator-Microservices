package com.sentinelorchestratorservice.Entity;

import com.sentinelorchestratorservice.Enum.WorkFlowStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="workflows")
public class WorkFlow {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "entity_sequence"
    )
    @SequenceGenerator(
            name = "entity_sequence",
            sequenceName = "entity_sequence",
            allocationSize = 1,
            initialValue = 20000
    )
    @Column(name="workflow_id",nullable = false,updatable = false)
    private long workFlowId;
    @Column(name="user_id")
    private Long userId;
    @Column(name="product_id")
    private Long productId;
    @Column(name="workflow_type")
    private String workFlowType;
    
    private int quantity;
    @Enumerated(EnumType.STRING)
    @Column(name="workflow_status")
    private WorkFlowStatus status;
    @Column(name="current_step")
    private String currentStep;
    @Lob
    private String payload;
    @Lob
    @Column(name="last_error")
    private String lastError;
    @Column(name="retries")
    private int retries;
    @Column(name="created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;
    @Column(name="updated_at",nullable = false)
    private LocalDateTime updatedAt;
    @Column(name="retried_at")
    private LocalDateTime retriedAt;

    @PrePersist
    public void onCreate(){
        LocalDateTime now=LocalDateTime.now();
        this.createdAt=now;
        this.updatedAt=now;
    }
    @PreUpdate
    public void onUpdate(){
        this.updatedAt=LocalDateTime.now();
    }


}
