package com.example.controller;

import com.example.dto.PageDto;
import com.example.dto.order.*;
import com.example.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageDto<OrderSummaryDto> getOrders(Pageable pageable,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) Long userId,
                                              Authentication authentication) {
        return orderService.getOrders(pageable, status, userId, authentication);
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequestDto dto,
                                Authentication authentication) {
        return ResponseEntity.status(201).body(orderService.createOrder(dto, authentication));
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id,
                             Authentication authentication) {
        return orderService.getOrder(id, authentication);
    }

    @PutMapping("/{id}")
    public OrderDto updateOrder(@PathVariable Long id,
                                @Valid @RequestBody OrderRequestDto dto,
                                Authentication authentication) {
        return orderService.updateOrder(id, dto, authentication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id,
                                            Authentication authentication) {
        orderService.deleteOrder(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public OrderDto updateStatus(@PathVariable Long id,
                                 @RequestBody OrderStatusDto statusDto,
                                 Authentication authentication) {
        return orderService.updateStatus(id, statusDto.getStatus(), authentication);
    }

    @PostMapping("/{orderId}/items")
    public OrderDto addOrderItem(@PathVariable Long orderId,
                                 @Valid @RequestBody OrderItemRequestDto itemDto,
                                 Authentication authentication) {
        return orderService.addOrderItem(orderId, itemDto, authentication);
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public OrderDto updateOrderItem(@PathVariable Long orderId,
                                    @PathVariable Long itemId,
                                    @Valid @RequestBody OrderItemRequestDto itemDto,
                                    Authentication authentication) {
        return orderService.updateOrderItem(orderId, itemId, itemDto, authentication);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public OrderDto deleteOrderItem(@PathVariable Long orderId,
                                    @PathVariable Long itemId,
                                    Authentication authentication) {
        return orderService.deleteOrderItem(orderId, itemId, authentication);
    }
}
