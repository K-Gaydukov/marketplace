package com.example.client;

import com.example.dto.PageDto;
import com.example.dto.order.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@Component
public class OrderClient {
    private final RestTemplate restTemplate;
    private final String orderUrl;

    public OrderClient(RestTemplate restTemplate,
                       @Value("${order.url}") String orderUrl) {
        this.restTemplate = restTemplate;
        this.orderUrl = orderUrl;
    }

    public PageDto<OrderSummaryDto> getOrders(String token, int page, int size, String status, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(orderUrl + "/orders")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParamIfPresent("status", Optional.ofNullable(status))
                .queryParamIfPresent("userId", Optional.ofNullable(userId))
                .toUriString();
        ResponseEntity<PageDto<OrderSummaryDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<PageDto<OrderSummaryDto>>() {});
        return response.getBody();
    }

    public OrderDto createOrder(String token, OrderRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(dto, headers);
        return restTemplate.postForObject(orderUrl + "/orders", entity, OrderDto.class);
    }

    public OrderDto getOrder(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                orderUrl + "/orders/" + id, HttpMethod.GET, entity, OrderDto.class).getBody();
    }

    public OrderDto updateOrder(String token, Long id, OrderRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(dto, headers);
        return restTemplate.exchange(
                orderUrl + "/orders/" + id, HttpMethod.PUT, entity, OrderDto.class).getBody();
    }

    public void deleteOrder(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        restTemplate.exchange(orderUrl + "/orders/" + id, HttpMethod.DELETE, entity, Void.class);
    }

    public OrderDto updateStatus(String token, Long id, OrderStatusDto statusDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderStatusDto> entity = new HttpEntity<>(statusDto, headers);
        return restTemplate.exchange(
                orderUrl + "/orders/" + id + "/status", HttpMethod.PUT, entity, OrderDto.class).getBody();
    }

    public OrderDto addOrderItem(String token, Long orderId, OrderItemRequestDto itemDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderItemRequestDto> entity = new HttpEntity<>(itemDto, headers);
        return restTemplate.postForObject(orderUrl + "/orders/" + orderId + "/items", entity, OrderDto.class);
    }

    public OrderDto updateOrderItem(String token, Long orderId, Long itemId, OrderItemRequestDto itemDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderItemRequestDto> entity = new HttpEntity<>(itemDto, headers);
        return restTemplate.exchange(
                orderUrl + "/orders/" + orderId + "/items/" + itemId,
                HttpMethod.PUT, entity, OrderDto.class).getBody();
    }

    public OrderDto deleteOrderItem(String token, Long orderId, Long itemId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                orderUrl + "/orders/" + orderId + "/items/" + itemId,
                HttpMethod.DELETE, entity, OrderDto.class).getBody();
    }
}
