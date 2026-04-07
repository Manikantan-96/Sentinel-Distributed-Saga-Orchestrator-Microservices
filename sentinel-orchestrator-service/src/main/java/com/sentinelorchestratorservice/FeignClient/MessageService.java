package com.sentinelorchestratorservice.FeignClient;

import com.sentinelorchestratorservice.DTO.Request.ServiceRequest;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="notification-service")
public interface MessageService {
    @RateLimiter(name = "myRateLimiter")
    @CircuitBreaker(name = "myCircuitBreakerNotification", fallbackMethod = "OrderSuccessFallback")
    @Bulkhead(name = "myBulkhead")
    @Retry(name = "myRetry")
    @PostMapping("/notification/send/order/success")
    public String sendNotificationForOrderSuccess(@RequestBody ServiceRequest serviceRequest);

    default String OrderSuccessFallback(ServiceRequest request, Throwable t) {
        return t.getMessage();
    }
}
