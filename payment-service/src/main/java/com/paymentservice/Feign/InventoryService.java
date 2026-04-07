package com.paymentservice.Feign;

import com.paymentservice.DTO.Request.ProductRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="inventory-service")
public interface InventoryService {

    @CircuitBreaker(name = "myCircuitBreakerInventory", fallbackMethod = "getProductDetailsFallback")
    @Retry(name = "myRetry")
    @GetMapping("/inventory/product/{productId}")
    ResponseEntity<ProductRequestDto> getProductDetails(@PathVariable Long productId);

    default ResponseEntity<ProductRequestDto> getProductDetailsFallback(Long productId, Throwable t) {
        ProductRequestDto fallbackProduct = new ProductRequestDto();
        fallbackProduct.setProductId(productId);
        fallbackProduct.setProductName("Unavailable Product");
        return ResponseEntity.ok(fallbackProduct);
    }
}
