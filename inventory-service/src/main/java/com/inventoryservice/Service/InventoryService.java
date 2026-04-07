package com.inventoryservice.Service;

import com.inventoryservice.DTO.Request.ServiceRequest;
import com.inventoryservice.DTO.Request.productRequestDto;
import com.inventoryservice.DTO.Response.InventoryResponse;
import com.inventoryservice.DTO.Response.ProductResponseDto;


public interface InventoryService {
     InventoryResponse reserveStock(ServiceRequest request);
     InventoryResponse releaseStock(Long workflowId);
     String getReservationDetails(String reservationId);
     ProductResponseDto getProductDetail(Long productId);
     ProductResponseDto createNewProduct(productRequestDto request);
     ProductResponseDto deleteProductDetail(Long productId);
     ProductResponseDto updateProduct(productRequestDto request, long productId);
}
