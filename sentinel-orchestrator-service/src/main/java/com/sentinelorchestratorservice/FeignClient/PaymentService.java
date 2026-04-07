package com.sentinelorchestratorservice.FeignClient;

import com.sentinelorchestratorservice.DTO.Request.ServiceRequest;
import com.sentinelorchestratorservice.DTO.Response.ServerResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="payment-service")
public interface PaymentService {

    @CircuitBreaker(name = "myCircuitBreakerPayment1", fallbackMethod = "processPaymentFallback")
    @Bulkhead(name= "myBulkhead")
    @Retry(name= "myRetry")
    @PostMapping("/payment/process")
    public ResponseEntity<ServerResponse> processPayment( @RequestBody ServiceRequest request);
    default ResponseEntity<ServerResponse> processPaymentFallback(ServiceRequest request, Throwable t){
        ServerResponse sr= new ServerResponse();
        sr.setStatus(t.getMessage());
        return ResponseEntity.ok(sr);
    }
    @CircuitBreaker(name = "myCircuitBreakerPayment2", fallbackMethod = "compensatePaymentFallback")
    @Bulkhead(name= "myBulkhead")
    @Retry(name= "myRetry")
    @GetMapping("/payment/compensate/{workflowId}")
    public ResponseEntity<ServerResponse> compensatePayment(@PathVariable Long workflowId);
    default ResponseEntity<ServerResponse> compensatePaymentFallback(Long workflowId, Throwable t){
        ServerResponse sr= new ServerResponse();
        sr.setStatus(t.getMessage());
        return ResponseEntity.ok(sr);
    }
}
