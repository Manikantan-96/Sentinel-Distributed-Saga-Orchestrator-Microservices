package com.inventoryservice.Entity;

import com.inventoryservice.Enum.StockStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="product_table")
public class Product {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "product_id", updatable = false, nullable = false)
    private Long productId;

    @Column(name = "product_name",nullable = false)
    private String productName;

    @Column(name = "price",nullable = false)
    private double price;

    @Column(name = "stock_quantity")
    private int stockQuantity;
    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private StockStatus status;

    @Column(name = "description")
    private String description;
    @Column(name="updated_at",nullable = false)
    private LocalDateTime updatedAt;
    @PrePersist
    @PreUpdate
    public void save(){
        this.updatedAt=LocalDateTime.now();
    }
}
