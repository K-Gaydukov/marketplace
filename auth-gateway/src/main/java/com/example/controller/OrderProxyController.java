package com.example.controller;

import com.example.client.OrderClient;
import com.example.dto.PageDto;
import com.example.dto.order.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderProxyController {
    private final OrderClient orderClient;

    public OrderProxyController(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
    }

    @GetMapping("/orders")
    public PageDto<OrderSummaryDto> getOrders(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) Long userId) {
        return orderClient.getOrders(getToken(request), page, size, status, userId);
    }

    @PostMapping("/orders")
    public OrderDto createOrder(HttpServletRequest request,
                                @RequestBody OrderRequestDto dto) {
        return orderClient.createOrder(getToken(request), dto);
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(HttpServletRequest request,
                             @PathVariable Long id) {
        return orderClient.getOrder(getToken(request), id);
    }

    @PutMapping("/orders/{id}")
    public OrderDto updateOrder(HttpServletRequest request,
                                @PathVariable Long id,
                                @RequestBody OrderRequestDto dto) {
        return orderClient.updateOrder(getToken(request), id, dto);
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(HttpServletRequest request,
                                            @PathVariable Long id) {
        orderClient.deleteOrder(getToken(request), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/orders/{id}/status")
    public OrderDto updateStatus(HttpServletRequest request,
                                 @PathVariable Long id,
                                 @RequestBody OrderStatusDto statusDto) {
        return orderClient.updateStatus(getToken(request), id, statusDto);
    }

    @PostMapping("/orders/{orderId}/items")
    public OrderDto addOrderItem(HttpServletRequest request,
                                 @PathVariable Long orderId,
                                 @RequestBody OrderItemRequestDto itemDto) {
        return orderClient.addOrderItem(getToken(request), orderId, itemDto);
    }

    @PutMapping("/orders/{orderId}/items/{itemId}")
    public OrderDto updateOrderItem(HttpServletRequest request,
                                    @PathVariable Long orderId,
                                    @PathVariable Long itemId,
                                    @RequestBody OrderItemRequestDto itemDto) {
        return orderClient.updateOrderItem(getToken(request), orderId, itemId, itemDto);
    }

    @DeleteMapping("/orders/{orderId}/items/{itemId}")
    public OrderDto deleteOrderItem(HttpServletRequest request,
                                    @PathVariable Long orderId,
                                    @PathVariable Long itemId) {
        return orderClient.deleteOrderItem(getToken(request), orderId, itemId);
    }
}
