package com.inventoryservice.Controller;

import com.inventoryservice.DTO.Request.ServiceRequest;
import com.inventoryservice.DTO.Request.productRequestDto;
import com.inventoryservice.DTO.Response.InventoryResponse;
import com.inventoryservice.DTO.Response.ProductResponseDto;
import com.inventoryservice.Service.InventoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/process")
    public ResponseEntity<InventoryResponse> reserveStock(@Valid @RequestBody ServiceRequest request) {
        log.info("Request received for reserve Stock: {}",request.toString());
        return ResponseEntity.ok(inventoryService.reserveStock(request));
    }

    @PostMapping("/compensate")
    public ResponseEntity<InventoryResponse> releaseStock(@RequestParam Long workflowId) {
        log.info("Request received for release Stock: {}",workflowId);
        return ResponseEntity.ok(inventoryService.releaseStock(workflowId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductResponseDto> getProductDetails(@PathVariable Long productId) {
        log.info("Request received for get ProductDetails: {}",productId);
        return ResponseEntity.ok(inventoryService.getProductDetail(productId));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<String> getReservationDetails(@PathVariable String reservationId) {
        log.info("Request received for get Reservation Details: {}",reservationId);
        return ResponseEntity.ok(inventoryService.getReservationDetails(reservationId));
    }
    @PostMapping("/product/create")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody productRequestDto request) {
        log.info("Request received for create Product: {}",request.toString());
        return ResponseEntity.ok(inventoryService.createNewProduct(request));
    }
    @PutMapping("/product/update")
    public ResponseEntity<ProductResponseDto> updateProduct(@Valid @RequestBody productRequestDto request,@RequestParam long productId) {
        log.info("Request received for update Product: {}",productId);
        return ResponseEntity.ok(inventoryService.updateProduct(request,productId));
    }
    @DeleteMapping("/product/delete/{productId}")
    public ResponseEntity<ProductResponseDto> deleteProductDetail(@PathVariable Long productId) {
        log.info("Request received for delete ProductDetail: {}",productId);
        return ResponseEntity.ok(inventoryService.deleteProductDetail(productId));
    }


}