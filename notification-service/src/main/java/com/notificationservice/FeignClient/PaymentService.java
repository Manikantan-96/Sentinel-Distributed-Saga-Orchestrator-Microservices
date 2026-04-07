package com.notificationservice.FeignClient;

import com.notificationservice.DTO.Request.UserRequsetDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="payment-service")
public interface PaymentService {
    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "getUserFallback")
    @Retry(name = "myRetry")
    @GetMapping("/payment/user/{userId}")
    ResponseEntity<UserRequsetDto> getUser(@PathVariable long userId);

    default ResponseEntity<UserRequsetDto> getUserFallback(long userId, Throwable t) {
        UserRequsetDto fallbackUser = new UserRequsetDto();
        fallbackUser.setUserId(userId);
        fallbackUser.setName("Unknown User");
        fallbackUser.setEmail("fallback@email.com");

        return ResponseEntity.ok(fallbackUser);
    }
}

