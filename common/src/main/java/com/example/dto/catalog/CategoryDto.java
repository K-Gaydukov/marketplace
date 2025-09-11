package com.example.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 128, message = "Name must be between 1 and 128 characters")
    private String name;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
