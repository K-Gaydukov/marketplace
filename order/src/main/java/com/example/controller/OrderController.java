package com.example.controller;

import com.example.dto.PageDto;
import com.example.dto.order.*;
import com.example.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
                                              Principal principal) {
        return orderService.getOrders(pageable, status, userId, (Authentication) principal);
    }

    @PostMapping
    public OrderDto createOrder(@Valid @RequestBody OrderRequestDto dto,
                                Principal principal) {
        return orderService.createOrder(principal.getName(), dto, (Authentication) principal);
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id,
                             Principal principal) {
        return orderService.getOrder(id, (Authentication) principal);
    }

    @PutMapping("/{id}")
    public OrderDto updateOrder(@PathVariable Long id,
                                @Valid @RequestBody OrderRequestDto dto,
                                Principal principal) {
        return orderService.updateOrder(id, dto, (Authentication) principal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id,
                                            Principal principal) {
        orderService.deleteOrder(id, (Authentication) principal);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public OrderDto updateStatus(@PathVariable Long id,
                                 @RequestBody OrderStatusDto statusDto,
                                 Principal principal) {
        return orderService.updateStatus(id, statusDto.getStatus(), (Authentication) principal);
    }

    @PostMapping("/{orderId}/items")
    public OrderDto addOrderItem(@PathVariable Long orderId,
                                 @Valid @RequestBody OrderItemRequestDto itemDto,
                                 Principal principal) {
        return orderService.addOrderItem(orderId, itemDto, (Authentication) principal);
    }

    @PutMapping("/{orderId}/items/{itemId}")
    public OrderDto updateOrderItem(@PathVariable Long orderId,
                                    @PathVariable Long itemId,
                                    @Valid @RequestBody OrderItemRequestDto itemDto,
                                    Principal principal) {
        return orderService.updateOrderItem(orderId, itemId, itemDto, (Authentication) principal);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public OrderDto deleteOrderItem(@PathVariable Long orderId,
                                    @PathVariable Long itemId,
                                    Principal principal) {
        return orderService.deleteOrderItem(orderId, itemId, (Authentication) principal);
    }
}
