package com.example.controller;

import com.example.config.SecurityConfig;
import com.example.dto.PageDto;
import com.example.dto.order.*;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.service.OrderService;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Claims userClaims;
    private Claims adminClaims;

    @BeforeEach
    void setUp() {
        userClaims = Jwts.claims()
                .setSubject("test-user")
                .add("role", "ROLE_USER")
                .add("uid", 1L)
                .add("fio", "Test User").build();

        adminClaims = Jwts.claims()
                .setSubject("test-admin")
                .add("role", "ROLE_ADMIN")
                .add("uid", 2L)
                .add("fio", "Test Admin").build();
    }

    @Test
    void getOrders_shouldReturn200_forUser() throws Exception {
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        when(orderService.getOrders(any(), eq(null), eq(null), any())).thenReturn(pageDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getOrders_shouldReturn200_forAdminWithFilters() throws Exception {
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        when(orderService.getOrders(any(), eq("NEW"), eq(1L), any())).thenReturn(pageDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "NEW")
                        .param("userId", "1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createOrder_shouldReturn201() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));
        OrderDto responseDto = new OrderDto();
        responseDto.setId(1L);
        when(orderService.createOrder(any(), any())).thenReturn(responseDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void createOrder_shouldReturn422_whenInvalid() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto(); // Пустой список items
        when(orderService.createOrder(any(), any())).thenThrow(new ValidationException("Order must have at least one item"));
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getOrder_shouldReturn200() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        when(orderService.getOrder(eq(1L), any())).thenReturn(orderDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/orders/{id}", 1L)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getOrder_shouldReturn404_whenNotFound() throws Exception {
        when(orderService.getOrder(eq(1L), any())).thenThrow(new NotFoundException("Order with id: 1 not found"));
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/orders/{id}", 1L)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order with id: 1 not found"));
    }

    @Test
    void updateOrder_shouldReturn200() throws Exception {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));
        OrderDto responseDto = new OrderDto();
        responseDto.setId(1L);
        when(orderService.updateOrder(eq(1L), any(), any())).thenReturn(responseDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(put("/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteOrder_shouldReturn204() throws Exception {
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(delete("/orders/{id}", 1L)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_shouldReturn200_forAdmin() throws Exception {
        OrderStatusDto statusDto = new OrderStatusDto();
        statusDto.setStatus("CONFIRMED");
        OrderDto responseDto = new OrderDto();
        responseDto.setId(1L);
        when(orderService.updateStatus(eq(1L), eq("CONFIRMED"), any())).thenReturn(responseDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(put("/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void addOrderItem_shouldReturn200() throws Exception {
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        OrderDto responseDto = new OrderDto();
        responseDto.setId(1L);
        when(orderService.addOrderItem(eq(1L), any(), any())).thenReturn(responseDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(post("/orders/{orderId}/items", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateOrderItem_shouldReturn200() throws Exception {
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        OrderDto responseDto = new OrderDto();
        responseDto.setId(1L);
        when(orderService.updateOrderItem(eq(1L), eq(1L), any(), any())).thenReturn(responseDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(put("/orders/{orderId}/items/{itemId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteOrderItem_shouldReturn200() throws Exception {
        OrderDto responseDto = new OrderDto();
        responseDto.setId(1L);
        when(orderService.deleteOrderItem(eq(1L), eq(1L), any())).thenReturn(responseDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(delete("/orders/{orderId}/items/{itemId}", 1L, 1L)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}
