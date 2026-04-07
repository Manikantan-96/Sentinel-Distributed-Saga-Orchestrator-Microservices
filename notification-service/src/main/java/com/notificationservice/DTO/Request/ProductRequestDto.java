package com.notificationservice.DTO.Request;

import com.notificationservice.Enum.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    private Long productId;
    private String productName;
    private double price;
    private int stockQuantity;
    private StockStatus status;
    private String description;
    private LocalDateTime updatedAt;
}
