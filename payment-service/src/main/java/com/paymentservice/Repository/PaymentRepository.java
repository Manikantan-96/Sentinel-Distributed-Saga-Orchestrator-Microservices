package com.paymentservice.Repository;

import com.paymentservice.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Payment findByWorkflowId(long workflowId);
    List<Payment> findByUserId(Long userId);
}
