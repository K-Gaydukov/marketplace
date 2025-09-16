package com.example.mapper;

import com.example.dto.order.OrderDto;
import com.example.dto.order.OrderSummaryDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderSummaryDto toSummaryDto(Order order);

    @Mapping(source = "items", target = "items")
    OrderDto toDto(Order order);

    OrderSummaryDto toItemDto(OrderItem item);
}
