package com.paymentservice.Entity;

import com.paymentservice.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="payment_table")
public class Payment {
    @Id
    @Column(name = "payment_id", updatable = false, nullable = false)
    private String paymentId;
    @Column(name = "workflow_id", nullable = false)
    private long workflowId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name="workflow_step_id")
    private String workFlowStepId;
    @Column(name="quantity")
    private int quantity;
    @Lob
    @Column(name="error_message")
    private String errorMessage;
    @Column(name="amount")
    private double amount;
    @Enumerated(EnumType.STRING)
    @Column(name="payment_status")
    private PaymentStatus paymentStatus;
    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    @PrePersist
    public void onCreated(){
        LocalDateTime now=LocalDateTime.now();
        this.createdAt=now;
        this.updatedAt=now;
    }
    @PreUpdate
    public void onUpdate(){
        this.updatedAt=LocalDateTime.now();
    }
}
