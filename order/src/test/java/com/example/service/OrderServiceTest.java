package com.example.service;

import com.example.client.CatalogClient;
import com.example.dto.PageDto;
import com.example.dto.catalog.ProductDto;
import com.example.dto.order.OrderDto;
import com.example.dto.order.OrderItemRequestDto;
import com.example.dto.order.OrderRequestDto;
import com.example.dto.order.OrderSummaryDto;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.OrderStatus;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.mapper.OrderMapper;
import com.example.repository.OrderItemRepository;
import com.example.repository.OrderRepository;
import com.example.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private OrderService orderService;

    private Authentication userAuth;
    private Authentication adminAuth;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        UserPrincipal userPrincipal = new UserPrincipal(1L, "test-user", "Test User", "ROLE_USER", "test-token");
        userAuth = mock(Authentication.class);
        doReturn(userPrincipal).when(userAuth).getPrincipal();
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(userAuth).getAuthorities();

        UserPrincipal adminPrincipal = new UserPrincipal(2L, "test-admin", "Test Admin", "ROLE_ADMIN", "test-token");
        adminAuth = mock(Authentication.class);
        doReturn(adminPrincipal).when(adminAuth).getPrincipal();
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(adminAuth).getAuthorities();

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Product");
        productDto.setPrice(BigDecimal.TEN);
        productDto.setStock(100);
        productDto.setActive(true);
    }

    // getOrders tests
    @Test
    void getOrders_shouldReturnOrders_forUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(page);
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        pageDto.setTotalElements(1);
        when(orderMapper.toSummaryDto(any())).thenReturn(new OrderSummaryDto());

        PageDto<OrderSummaryDto> result = orderService.getOrders(pageable, null, null, userAuth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByUserId(1L, pageable);
    }

    @Test
    void getOrders_shouldReturnOrders_forUserWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.NEW, pageable)).thenReturn(page);
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        pageDto.setTotalElements(1);
        when(orderMapper.toSummaryDto(any())).thenReturn(new OrderSummaryDto());

        PageDto<OrderSummaryDto> result = orderService.getOrders(pageable, "NEW", null, userAuth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByUserIdAndStatus(1L, OrderStatus.NEW, pageable);
    }

    @Test
    void getOrders_shouldReturnOrders_forAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        pageDto.setTotalElements(1);
        when(orderMapper.toSummaryDto(any())).thenReturn(new OrderSummaryDto());

        PageDto<OrderSummaryDto> result = orderService.getOrders(pageable, null, null, adminAuth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findAll(any(Pageable.class));
    }

    @Test
    void getOrders_shouldReturnOrders_forAdminWithStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByStatus(OrderStatus.NEW, pageable)).thenReturn(page);
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        pageDto.setTotalElements(1);
        when(orderMapper.toSummaryDto(any())).thenReturn(new OrderSummaryDto());

        PageDto<OrderSummaryDto> result = orderService.getOrders(pageable, "NEW", null, adminAuth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByStatus(OrderStatus.NEW, pageable);
    }

    @Test
    void getOrders_shouldReturnOrders_forAdminWithUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUserId(999L, pageable)).thenReturn(page);
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        pageDto.setTotalElements(1);
        when(orderMapper.toSummaryDto(any())).thenReturn(new OrderSummaryDto());

        PageDto<OrderSummaryDto> result = orderService.getOrders(pageable, null, 999L, adminAuth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByUserId(999L, pageable);
    }

    @Test
    void getOrders_shouldReturnOrders_forAdminWithUserIdAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Order order = new Order();
        order.setId(1L);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(orderRepository.findByUserIdAndStatus(999L, OrderStatus.NEW, pageable)).thenReturn(page);
        PageDto<OrderSummaryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new OrderSummaryDto()));
        pageDto.setTotalElements(1);
        when(orderMapper.toSummaryDto(any())).thenReturn(new OrderSummaryDto());

        PageDto<OrderSummaryDto> result = orderService.getOrders(pageable, "NEW", 999L, adminAuth);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findByUserIdAndStatus(999L, OrderStatus.NEW, pageable);
    }

    // createOrder tests
    @Test
    void createOrder_shouldReturnOrder() {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setItems(new ArrayList<>());
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());

        OrderDto result = orderService.createOrder(requestDto, userAuth);

        assertThat(result).isNotNull();
        verify(catalogClient).getProduct(1L, "test-token");
        verify(orderRepository).save(any());
        verify(orderItemRepository, times(1)).save(any());
    }

    @Test
    void createOrder_shouldThrowValidationException_whenEmptyItems() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setItems(List.of());

        assertThrows(ValidationException.class, () -> orderService.createOrder(requestDto, userAuth));
    }

    @Test
    void createOrder_shouldThrowValidationException_whenItemsNull() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setItems(null);

        assertThrows(ValidationException.class, () -> orderService.createOrder(requestDto, userAuth));
    }

    @Test
    void createOrder_shouldThrowValidationException_whenProductInactive() {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));
        ProductDto inactiveProduct = new ProductDto();
        inactiveProduct.setId(1L);
        inactiveProduct.setName("Product");
        inactiveProduct.setPrice(BigDecimal.TEN);
        inactiveProduct.setStock(100);
        inactiveProduct.setActive(false);
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(inactiveProduct);

        assertThrows(ValidationException.class, () -> orderService.createOrder(requestDto, userAuth));
    }

    @Test
    void createOrder_shouldThrowValidationException_whenInsufficientStock() {
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(200);
        requestDto.setItems(List.of(itemDto));
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);

        assertThrows(ValidationException.class, () -> orderService.createOrder(requestDto, userAuth));
    }

    // getOrder tests
    @Test
    void getOrder_shouldReturnOrder_forOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());

        OrderDto result = orderService.getOrder(1L, userAuth);

        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrder_shouldThrowValidationException_forNonOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> orderService.getOrder(1L, userAuth));
    }

    @Test
    void getOrder_shouldReturnOrder_forAdmin() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());

        OrderDto result = orderService.getOrder(1L, adminAuth);

        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrder_shouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrder(1L, userAuth));
    }

    // updateOrder tests
    @Test
    void updateOrder_shouldReturnUpdatedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(new ArrayList<>());
        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        order.getItems().add(item);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);
        when(catalogClient.updateStock(1L, 2, "test-token")).thenReturn(productDto);
        when(catalogClient.updateStock(1L, -3, "test-token")).thenReturn(productDto);
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());
        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(3);
        requestDto.setItems(List.of(itemDto));

        OrderDto result = orderService.updateOrder(1L, requestDto, userAuth);

        assertThat(result).isNotNull();
        verify(catalogClient).updateStock(1L, 2, "test-token"); // Откат
        verify(catalogClient).updateStock(1L, -3, "test-token"); // Новый
    }

    @Test
    void updateOrder_shouldThrowValidationException_whenEmptyItems() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setItems(List.of());

        assertThrows(ValidationException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    @Test
    void updateOrder_shouldThrowValidationException_whenItemsNull() {
        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setItems(null);

        assertThrows(ValidationException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    @Test
    void updateOrder_shouldThrowValidationException_forNonOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));

        assertThrows(ValidationException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    @Test
    void updateOrder_shouldThrowValidationException_whenNotNewStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));

        assertThrows(ValidationException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    @Test
    void updateOrder_shouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));

        assertThrows(NotFoundException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    @Test
    void updateOrder_shouldThrowValidationException_whenProductInactive() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        ProductDto inactiveProduct = new ProductDto();
        inactiveProduct.setId(1L);
        inactiveProduct.setName("Product");
        inactiveProduct.setPrice(BigDecimal.TEN);
        inactiveProduct.setStock(100);
        inactiveProduct.setActive(false);
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(inactiveProduct);

        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);
        requestDto.setItems(List.of(itemDto));

        assertThrows(ValidationException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    @Test
    void updateOrder_shouldThrowValidationException_whenInsufficientStock() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);

        OrderRequestDto requestDto = new OrderRequestDto();
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(200);
        requestDto.setItems(List.of(itemDto));

        assertThrows(ValidationException.class, () -> orderService.updateOrder(1L, requestDto, userAuth));
    }

    // deleteOrder tests
    @Test
    void deleteOrder_shouldSucceed_forOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(List.of(new OrderItem()));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrder(1L, userAuth);

        verify(orderItemRepository).deleteAll(any());
        verify(orderRepository).delete(any());
    }

    @Test
    void deleteOrder_shouldThrowValidationException_forNonOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> orderService.deleteOrder(1L, userAuth));
    }

    @Test
    void deleteOrder_shouldThrowValidationException_whenNotNewStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> orderService.deleteOrder(1L, userAuth));
    }

    @Test
    void deleteOrder_shouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.deleteOrder(1L, userAuth));
    }

    // updateStatus tests
    @Test
    void updateStatus_shouldReturnUpdatedOrder_forAdmin() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setStatus(String.valueOf(OrderStatus.COMPLETED));
        when(orderMapper.toDto(any())).thenReturn(orderDto);

        OrderDto result = orderService.updateStatus(1L, String.valueOf(OrderStatus.COMPLETED), adminAuth);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(String.valueOf(OrderStatus.COMPLETED));
        verify(orderRepository).save(any());
    }

    @Test
    void updateStatus_shouldThrowAccessDenied_forUser() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> orderService.updateStatus(1L, "CONFIRMED", userAuth));
    }

    @Test
    void updateStatus_shouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.updateStatus(1L, "CONFIRMED", adminAuth));
    }

    @Test
    void updateStatus_shouldThrowIllegalArgumentException_forInvalidStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.updateStatus(1L, "INVALID", adminAuth));
    }

    // addOrderItem tests
    @Test
    void addOrderItem_shouldReturnUpdatedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);
        when(catalogClient.updateStock(1L, -2, "test-token")).thenReturn(productDto);
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        OrderDto result = orderService.addOrderItem(1L, itemDto, userAuth);

        assertThat(result).isNotNull();
        verify(orderItemRepository).save(any());
        verify(catalogClient).updateStock(1L, -2, "test-token");
    }

    @Test
    void addOrderItem_shouldThrowValidationException_forNonOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(ValidationException.class, () -> orderService.addOrderItem(1L, itemDto, userAuth));
    }

    @Test
    void addOrderItem_shouldThrowValidationException_whenNotNewStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(ValidationException.class, () -> orderService.addOrderItem(1L, itemDto, userAuth));
    }

    @Test
    void addOrderItem_shouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(NotFoundException.class, () -> orderService.addOrderItem(1L, itemDto, userAuth));
    }

    @Test
    void addOrderItem_shouldThrowValidationException_whenProductInactive() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        ProductDto inactiveProduct = new ProductDto();
        inactiveProduct.setId(1L);
        inactiveProduct.setName("Product");
        inactiveProduct.setPrice(BigDecimal.TEN);
        inactiveProduct.setStock(100);
        inactiveProduct.setActive(false);
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(inactiveProduct);

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(ValidationException.class, () -> orderService.addOrderItem(1L, itemDto, userAuth));
    }

    @Test
    void addOrderItem_shouldThrowValidationException_whenInsufficientStock() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(200);

        assertThrows(ValidationException.class, () -> orderService.addOrderItem(1L, itemDto, userAuth));
    }

    // updateOrderItem tests
    @Test
    void updateOrderItem_shouldReturnUpdatedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        order.setItems(new ArrayList<>(List.of(item)));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(productDto);
        when(catalogClient.updateStock(1L, 2, "test-token")).thenReturn(productDto);
        when(catalogClient.updateStock(1L, -3, "test-token")).thenReturn(productDto);
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());
        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(3);

        OrderDto result = orderService.updateOrderItem(1L, 1L, itemDto, userAuth);

        assertThat(result).isNotNull();
        verify(orderItemRepository).save(any());
    }

    @Test
    void updateOrderItem_shouldThrowValidationException_forNonOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(ValidationException.class, () -> orderService.updateOrderItem(1L, 1L, itemDto, userAuth));
    }

    @Test
    void updateOrderItem_shouldThrowValidationException_whenNotNewStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(ValidationException.class, () -> orderService.updateOrderItem(1L, 1L, itemDto, userAuth));
    }

    @Test
    void updateOrderItem_shouldThrowNotFoundException_forOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(NotFoundException.class, () -> orderService.updateOrderItem(1L, 1L, itemDto, userAuth));
    }

    @Test
    void updateOrderItem_shouldThrowNotFoundException_forItem() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(NotFoundException.class, () -> orderService.updateOrderItem(1L, 1L, itemDto, userAuth));
    }

    @Test
    void updateOrderItem_shouldThrowValidationException_whenProductInactive() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        order.setItems(new ArrayList<>(List.of(item)));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        ProductDto inactiveProduct = new ProductDto();
        inactiveProduct.setId(1L);
        inactiveProduct.setName("Product");
        inactiveProduct.setPrice(BigDecimal.TEN);
        inactiveProduct.setStock(100);
        inactiveProduct.setActive(false);
        when(catalogClient.getProduct(1L, "test-token")).thenReturn(inactiveProduct);

        OrderItemRequestDto itemDto = new OrderItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(2);

        assertThrows(ValidationException.class, () -> orderService.updateOrderItem(1L, 1L, itemDto, userAuth));
    }

    // deleteOrderItem tests
    @Test
    void deleteOrderItem_shouldReturnUpdatedOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        order.setItems(new ArrayList<>(List.of(item)));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(catalogClient.updateStock(1L, 2, "test-token")).thenReturn(productDto);
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(new OrderDto());

        OrderDto result = orderService.deleteOrderItem(1L, 1L, userAuth);

        assertThat(result).isNotNull();
        verify(orderItemRepository).delete(any());
        verify(catalogClient).updateStock(1L, 2, "test-token");
    }

    @Test
    void deleteOrderItem_shouldThrowValidationException_forNonOwner() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(999L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> orderService.deleteOrderItem(1L, 1L, userAuth));
    }

    @Test
    void deleteOrderItem_shouldThrowValidationException_whenNotNewStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ValidationException.class, () -> orderService.deleteOrderItem(1L, 1L, userAuth));
    }

    @Test
    void deleteOrderItem_shouldThrowNotFoundException_forOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.deleteOrderItem(1L, 1L, userAuth));
    }

    @Test
    void deleteOrderItem_shouldThrowNotFoundException_forItem() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.NEW);
        order.setItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(NotFoundException.class, () -> orderService.deleteOrderItem(1L, 1L, userAuth));
    }
}
