package com.inventoryservice.Feign;

import com.inventoryservice.DTO.Request.ServiceRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="notification-service")
public interface NotificationService {
    //@Async
    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "NotificationFallback")
    @Retry(name = "myRetry")
    @PostMapping("/notification/send/failed/outofstock")
    public String sendNotificationForFailedOutOfStock(@RequestBody ServiceRequest serviceRequest);
    default String NotificationFallback(ServiceRequest serviceRequest, Throwable t) {
        return "Notification service is not available at this moment try again after some time";
    }
}
