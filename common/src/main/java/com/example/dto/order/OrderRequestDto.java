package com.example.dto.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDto {
    private List<OrderItemRequestDto> items;
}
