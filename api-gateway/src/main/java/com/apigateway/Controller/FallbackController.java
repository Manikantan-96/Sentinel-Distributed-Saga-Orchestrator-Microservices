package com.apigateway.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
    @GetMapping("/fallback/payment")
    public String paymentFallback() {
        return "Payment Service is currently unavailable please try again after some time";
    }
    @GetMapping("/fallback/inventory")
    public String inventoryFallback() {
        return "Inventory Service is currently unavailable please try again after some time";
    }
    @GetMapping("/fallback/sentinel")
    public String sentinelFallback() {
        return "Sentinel Service is currently unavailable please try again after some time";
    }
}
