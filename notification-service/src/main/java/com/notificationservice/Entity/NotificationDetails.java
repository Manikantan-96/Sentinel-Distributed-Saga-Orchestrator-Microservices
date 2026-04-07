package com.notificationservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification_details")
public class NotificationDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;
    @Column(name = "workflow_id")
    private Long workflowId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "status")
    private String status;
    @Column(name = "error_message")
    private String errorMessage;
    @Column(name="workflow_step_id")
    private String WorkFlowStepId;
    @Column(name = "amount")
    private Double amount;
    @Column(name = "called_timer")
    private LocalDateTime calledTimer;

}
