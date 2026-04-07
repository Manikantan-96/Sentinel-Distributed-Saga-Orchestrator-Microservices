package com.sentinelorchestratorservice.FeignClient;

import com.sentinelorchestratorservice.DTO.Request.ServiceRequest;
import com.sentinelorchestratorservice.DTO.Response.ServerResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="inventory-service")
public interface InventoryService {

    @CircuitBreaker(name = "myCircuitBreakerInventory1", fallbackMethod = "reserveStockFallback")
    @Bulkhead(name= "myBulkhead")
    @Retry(name= "myRetry")
    @PostMapping("/inventory/process")
    public ResponseEntity<ServerResponse> reserveStock( @RequestBody ServiceRequest request);
    default ResponseEntity<ServerResponse> reserveStockFallback(ServiceRequest request, Throwable t) {
        ServerResponse sr= new ServerResponse();
        sr.setStatus(t.getMessage());
        return ResponseEntity.ok(sr);
    }
    @CircuitBreaker(name = "myCircuitBreakerInventory2", fallbackMethod = "releaseStockFallback")
    @Bulkhead(name= "myBulkhead")
    @Retry(name= "myRetry")
    @PostMapping("/inventory/compensate")
    public ResponseEntity<ServerResponse> releaseStock(@RequestParam Long workflowId);
    default ResponseEntity<ServerResponse> releaseStockFallback(Long workflowId, Throwable t){
        ServerResponse sr= new ServerResponse();
        sr.setStatus(t.getMessage());
        return ResponseEntity.ok(sr);
    }
}