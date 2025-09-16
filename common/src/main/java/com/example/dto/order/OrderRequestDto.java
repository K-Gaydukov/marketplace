package com.example.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull(message = "Items cannot be null")
    @NotEmpty(message = "Items cannot be empty")
    private List<OrderItemRequestDto> items;
}
