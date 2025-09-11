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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final CatalogClient catalogClient;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        OrderMapper orderMapper,
                        CatalogClient catalogClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.catalogClient = catalogClient;
    }

    @Transactional(readOnly = true)
    public PageDto<OrderSummaryDto> getOrders(Pageable pageable, String status, Long userId, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_ADMIN"));
        Page<Order> orders;
        if (isAdmin) {
            if (userId != null) {
                orders = status != null
                        ? orderRepository.findByUserIdAndStatus(userId, OrderStatus.valueOf(status.toUpperCase()), pageable)
                        : orderRepository.findByUserId(userId, pageable);
            } else {
                orders = status != null
                        ? orderRepository.findByStatus(OrderStatus.valueOf(status), pageable)
                        : orderRepository.findAll(pageable);
            }
        } else {
            orders = status != null
                    ? orderRepository.findByUserIdAndStatus(principalId, OrderStatus.valueOf(status), pageable)
                    : orderRepository.findByUserId(principalId, pageable);
        }
        Page<OrderSummaryDto> orderSummaryPage = orders.map(orderMapper::toSummaryDto);
        return new PageDto<>(orderSummaryPage);
    }

    public OrderDto createOrder(OrderRequestDto dto, Authentication auth) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = principal.getToken();
        Order order = new Order();
        order.setUserId(principal.getUserId());
        order.setUserFio(principal.getFullName());
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        for (OrderItemRequestDto itemDto : dto.getItems()) {
            ProductDto product = catalogClient.getProduct(itemDto.getProductId(), token);
            if (!product.getIsActive()) {
                throw new ValidationException("Product " + itemDto.getProductId() + " is inactive");
            }
            if (product.getStock() < itemDto.getQuantity()) {
                throw new ValidationException("Insufficient stock for product " + itemDto.getProductId());
            }
            // Снимок (snapshot) цены и названия
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItemRepository.save(item);
            // Резервирование склада
            catalogClient.updateStock(product.getId(), - itemDto.getQuantity(), token);
            order.setTotalAmount(order.getTotalAmount().add(item.getLineTotal()));
        }
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long id, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        return orderMapper.toDto(order);
    }

    public OrderDto updateOrder(Long id, OrderRequestDto dto, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        String token = principal.getToken();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only update orders with status NEW");
        }
        // Удаляем старые items и откатываем stock
        List<OrderItem> oldItems = orderItemRepository.findByOrderId(id);
        for (OrderItem item : oldItems) {
            catalogClient.updateStock(item.getProductId(), item.getQuantity(), token);
            orderItemRepository.delete(item);
        }
        // Добавляем новые
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequestDto itemDto : dto.getItems()) {
            ProductDto product = catalogClient.getProduct(itemDto.getProductId(), token);
            if (!product.getIsActive()) {
                throw new ValidationException("Product " + itemDto.getProductId() + " is inactive");
            }
            if (product.getStock() < itemDto.getQuantity()) {
                throw new ValidationException("Insufficient stock for product " + itemDto.getProductId());
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemDto.getProductId());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItemRepository.save(item);
            catalogClient.updateStock(itemDto.getProductId(), -itemDto.getQuantity(), token);
            totalAmount = totalAmount.add(item.getLineTotal());
        }
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public void deleteOrder(Long id, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        String token = principal.getToken();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only delete orders with status NEW");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        for (OrderItem item : items) {
            catalogClient.updateStock(item.getProductId(), item.getQuantity(), token);
            orderItemRepository.delete(item);
        }
        orderRepository.delete(order);
    }

    public OrderDto updateStatus(Long id, String status, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (!isAdmin) {
            throw new ValidationException("Only admins can update order status");
        }
        try {
            order.setStatus(OrderStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + status);
        }
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto addOrderItem(Long orderId, OrderItemRequestDto itemDto, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        String token = principal.getToken();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id: " + orderId + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only add items to orders with status NEW");
        }
        ProductDto product = catalogClient.getProduct(itemDto.getProductId(), token);
        if (!product.getIsActive()) {
            throw new ValidationException("Product " + itemDto.getProductId() + " is inactive");
        }
        if (product.getStock() < itemDto.getQuantity()) {
            throw new ValidationException("Insufficient stock for product " + itemDto.getProductId());
        }
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(itemDto.getProductId());
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(itemDto.getQuantity());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        orderItemRepository.save(item);
        catalogClient.updateStock(itemDto.getProductId(), -itemDto.getQuantity(), token);
        BigDecimal totalAmount = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto updateOrderItem(Long orderId, Long itemId, OrderItemRequestDto itemDto, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        String token = principal.getToken();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id: " + orderId + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only update items in orders with status NEW");
        }
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id: " + itemId + " not found"));
        if (!item.getOrder().getId().equals(orderId)) {
            throw new ValidationException("Item does not belong to order");
        }
        ProductDto product = catalogClient.getProduct(item.getProductId(), token);
        if (!product.getIsActive()) {
            throw new ValidationException("Product " + item.getProductId() + " is inactive");
        }
        // Откат старого запаса
        catalogClient.updateStock(item.getProductId(), item.getQuantity(), token);
        // Новый снимок
        item.setOrder(order);
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(itemDto.getQuantity());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        orderItemRepository.save(item);
        // Новый запас
        catalogClient.updateStock(item.getProductId(), -itemDto.getQuantity(), token);
        // Пересчет totalAmount
        BigDecimal totalAmount = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto deleteOrderItem(Long orderId, Long itemId, Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        Long principalId = principal.getUserId();
        String token = principal.getToken();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id: " + orderId + " not found"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only delete items from orders with status NEW");
        }
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id: " + itemId + " not found"));
        if (!item.getOrder().getId().equals(orderId)) {
            throw new ValidationException("Item does not belong to order");
        }
        // Откат запаса
        catalogClient.updateStock(item.getProductId(), item.getQuantity(), token);
        orderItemRepository.delete(item);
        BigDecimal totalAmount = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }
}
