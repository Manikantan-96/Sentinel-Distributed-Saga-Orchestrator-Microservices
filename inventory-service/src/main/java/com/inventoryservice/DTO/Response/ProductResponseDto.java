package com.inventoryservice.DTO.Response;

import com.inventoryservice.Enum.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto implements Serializable {
    private Long productId;
    private String productName;
    private double price;
    private int stockQuantity;
    private StockStatus status;
    private String description;
    private LocalDateTime updatedAt;
}
