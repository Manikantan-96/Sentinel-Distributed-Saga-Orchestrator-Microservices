package com.inventoryservice.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class productRequestDto {
    @NotBlank
    private String productName;
    @Positive
    @NotNull
    private double price;
    @NotNull
    @Min(1)
    private int stockQuantity;
    @NotNull
    private String status;
    @NotBlank
    private String description;
}
