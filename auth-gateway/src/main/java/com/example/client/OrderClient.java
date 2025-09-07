package com.example.client;

import com.example.dto.PageDto;
import com.example.dto.order.*;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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
        try {
            ResponseEntity<PageDto<OrderSummaryDto>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<PageDto<OrderSummaryDto>>() {});
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Orders not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid request parameters");
            } else {
                throw new RuntimeException("Order service error: " + e.getMessage());
            }
        }
    }

    public OrderDto createOrder(String token, OrderRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.postForObject(orderUrl + "/orders", entity, OrderDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid order data");
            } else {
                throw new RuntimeException("Order creation error: " + e.getMessage());
            }
        }
    }

    public OrderDto getOrder(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(
                    orderUrl + "/orders/" + id, HttpMethod.GET, entity, OrderDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Access denied or invalid order");
            } else {
                throw new RuntimeException("Order service error: " + e.getMessage());
            }
        }
    }

    public OrderDto updateOrder(String token, Long id, OrderRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(dto, headers);
        try {
            return restTemplate.exchange(
                    orderUrl + "/orders/" + id, HttpMethod.PUT, entity, OrderDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid update or order not in NEW status");
            } else {
                throw new RuntimeException("Order update error: " + e.getMessage());
            }
        }
    }

    public void deleteOrder(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(orderUrl + "/orders/" + id, HttpMethod.DELETE, entity, Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Cannot delete order");
            } else {
                throw new RuntimeException("Order deletion error: " + e.getMessage());
            }
        }
    }

    public OrderDto updateStatus(String token, Long id, OrderStatusDto statusDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderStatusDto> entity = new HttpEntity<>(statusDto, headers);
        try {
            return restTemplate.exchange(
                    orderUrl + "/orders/" + id + "/status", HttpMethod.PUT, entity, OrderDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid status update");
            } else {
                throw new RuntimeException("Order status update error: " + e.getMessage());
            }
        }
    }

    public OrderDto addOrderItem(String token, Long orderId, OrderItemRequestDto itemDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderItemRequestDto> entity = new HttpEntity<>(itemDto, headers);
        try {
            return restTemplate.postForObject(orderUrl + "/orders/" + orderId + "/items", entity, OrderDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order or product not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid item or order not in NEW status");
            } else {
                throw new RuntimeException("Order item creation error: " + e.getMessage());
            }
        }
    }

    public OrderDto updateOrderItem(String token, Long orderId, Long itemId, OrderItemRequestDto itemDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderItemRequestDto> entity = new HttpEntity<>(itemDto, headers);
        try {
            return restTemplate.exchange(
                    orderUrl + "/orders/" + orderId + "/items/" + itemId,
                    HttpMethod.PUT, entity, OrderDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order or item not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid item update or order not in NEW status");
            } else {
                throw new RuntimeException("Order item update error: " + e.getMessage());
            }
        }
    }

    public OrderDto deleteOrderItem(String token, Long orderId, Long itemId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(
                    orderUrl + "/orders/" + orderId + "/items/" + itemId,
                    HttpMethod.DELETE, entity, OrderDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order or item not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Cannot delete item");
            } else {
                throw new RuntimeException("Order item deletion error: " + e.getMessage());
            }
        }
    }
}
