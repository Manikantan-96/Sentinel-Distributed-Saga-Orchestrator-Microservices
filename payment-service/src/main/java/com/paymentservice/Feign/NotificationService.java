package com.paymentservice.Feign;

import com.paymentservice.DTO.Request.ServiceRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="notification-service")
public interface NotificationService {
    @CircuitBreaker(name = "myCircuitBreakerPaymentRefunded", fallbackMethod = "PaymentRefundedFallback")
    @Retry(name = "myRetry")
    @PostMapping("/notification/send/payment/refunded")
    public String sendNotificationForPaymentRefunded(@RequestBody ServiceRequest serviceRequest);
    default String PaymentRefundedFallback(ServiceRequest serviceRequest, Throwable t) {
        return "Notification service is not available to send PaymentRefunded Email";
    }

    @CircuitBreaker(name = "myCircuitBreakerPaymentSuccess", fallbackMethod = "PaymentSuccessFallback")
    @Retry(name = "myRetry")
    @PostMapping("/notification/send/payment/success")
    public String sendNotificationForPaymentSuccess(@RequestBody ServiceRequest serviceRequest);
    default String PaymentSuccessFallback(ServiceRequest serviceRequest, Throwable t) {
        return "Notification service is not available to send PaymentSuccess Email";
    }

    @CircuitBreaker(name = "myCircuitBreakerFailedBalance", fallbackMethod = "FailedBalanceFallback")
    @Retry(name = "myRetry")
    @PostMapping("/notification/send/failed/balance")
    public String sendNotificationForFailedBalance(@RequestBody ServiceRequest serviceRequest);
    default String FailedBalanceFallback(ServiceRequest serviceRequest, Throwable t) {
        return "Notification service is not available to send FailedBalance Email";
    }
}
