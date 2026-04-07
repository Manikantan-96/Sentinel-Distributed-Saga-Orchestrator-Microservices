package com.paymentservice.Service;

import com.paymentservice.DTO.Request.ServiceRequest;
import com.paymentservice.DTO.Response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    public PaymentResponse processPayment(ServiceRequest request);
    public PaymentResponse getPaymentByWorkflowId(long workflowId);
    public List<PaymentResponse> getPaymentByUserId(Long userId);
    public PaymentResponse compensatePayment(Long workflowId);

}
