package com.paymentservice.DTO.Request;


import com.paymentservice.Enum.StockStatus;
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
