package com.example.client;

import com.example.dto.PageDto;
import com.example.dto.order.*;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@Component
public class OrderClient {
    private final RestTemplate restTemplate;
    private final String orderUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderClient(RestTemplate restTemplate,
                       @Value("${order.url}") String orderUrl) {
        this.restTemplate = restTemplate;
        this.orderUrl = orderUrl;
    }

    private String extractMessage(String body) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(body, Map.class);
            return (String) jsonMap.getOrDefault("message", body);
        } catch (IOException e) {
            return body;
        }
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
            }
        }
    }

    public OrderDto createOrder(String token, OrderRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<OrderRequestDto> entity = new HttpEntity<>(dto, headers);
        try {
            return restTemplate.postForObject(orderUrl + "/orders", entity, OrderDto.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Order creation failed");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
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
                throw new ValidationException(extractMessage(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(extractMessage(e.getResponseBodyAsString()));
            }
        }
    }
}
