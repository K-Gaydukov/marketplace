package com.example.dto.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDto {
    private Long id;

    @NotBlank(message = "SKU cannot be empty")
    private String sku;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private boolean isActive;

    @NotNull(message = "Category ID cannot be empty")
    private Long categoryId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
