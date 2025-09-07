package com.example.service;

import com.example.client.CatalogClient;
import com.example.dto.PageDto;
import com.example.dto.PageableDto;
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
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(
                a -> a.getAuthority().equals("ROLE_ADMIN"));
        Page<Order> orders;
        if (isAdmin && userId != null) {
            orders = status != null
                    ? orderRepository.findByUserIdAndStatus(userId, OrderStatus.valueOf(status), pageable)
                    : orderRepository.findByUserId(userId, pageable);
        } else {
            orders = status != null
                    ? orderRepository.findByUserIdAndStatus(principalId, OrderStatus.valueOf(status), pageable)
                    : orderRepository.findByUserId(principalId, pageable);
        }
        List<OrderSummaryDto> dtos = orders.getContent().stream()
                .map(orderMapper::toSummaryDto)
                .toList();
        return new PageDto<>(dtos, new PageableDto(orders.getPageable()), orders.isLast(), orders.getTotalPages(), orders.getTotalElements());
    }

    public OrderDto createOrder(String token, OrderRequestDto dto, Authentication auth) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }
        Order order = new Order();
        order.setUserId(Long.parseLong(auth.getName()));
        order.setUserFio(auth.getDetails().toString());
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
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        return orderMapper.toDto(order);
    }

    public OrderDto updateOrder(Long id, OrderRequestDto dto, Authentication auth) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only update orders with status NEW");
        }
        orderItemRepository.deleteByOrderId(id);  // Удаляем старые позиции
        order.setTotalAmount(BigDecimal.ZERO);
        for (OrderItemRequestDto itemDto : dto.getItems()) {
            ProductDto product = catalogClient.getProduct(itemDto.getProductId(), auth.getName());
            if (!product.getIsActive()) {
                throw new ValidationException("Product " + itemDto.getProductId() + " is inactive");
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(itemDto.getQuantity());
            item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItemRepository.save(item);

            catalogClient.updateStock(product.getId(), - itemDto.getQuantity(), auth.getName());
            order.setTotalAmount(order.getTotalAmount().add(item.getLineTotal()));
        }
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public void deleteOrder(Long id, Authentication auth) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only delete orders with status NEW");
        }
        orderItemRepository.deleteByOrderId(id);
        orderRepository.delete(order);
    }

    public OrderDto updateStatus(Long id, String status, Authentication auth) {
        if (auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ValidationException("Only admins can update order status");
        }
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        order.setStatus(OrderStatus.valueOf(status));
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto addOrderItem(Long id, OrderItemRequestDto itemDto, Authentication auth) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with id: " + id + " not found"));
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Only admins can update order status");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only add items to orders with status NEW");
        }
        ProductDto product = catalogClient.getProduct(itemDto.getProductId(), auth.getName());
        if (!product.getIsActive()) {
            throw new ValidationException("Product " + itemDto.getProductId() + " is inactive");
        }
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(itemDto.getQuantity());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        orderItemRepository.save(item);

        catalogClient.updateStock(product.getId(), - itemDto.getQuantity(), auth.getName());
        order.setTotalAmount(order.getTotalAmount().add(item.getLineTotal()));
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto updateOrderItem(Long orderId, Long itemId, OrderItemRequestDto itemDto, Authentication auth) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id: " + orderId + " not found"));
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !order.getUserId().equals(principalId)) {
            throw new ValidationException("Access denied: not your order");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ValidationException("Can only update items in orders with status NEW");
        }

        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id: " + itemId + " not found"));
        if (item.getOrder().getId().equals(orderId)) {
            throw new ValidationException("Item does not belong to order");
        }
        ProductDto product = catalogClient.getProduct(item.getProductId(), auth.getName());
        if (!product.getIsActive()) {
            throw new ValidationException("Product " + item.getProductId() + " is inactive");
        }
        // Откат старого запаса
        catalogClient.updateStock(item.getProductId(), item.getQuantity(), auth.getName());
        // Новый снимок
        item.setOrder(order);
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setQuantity(itemDto.getQuantity());
        item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        orderItemRepository.save(item);
        // Новый запас
        catalogClient.updateStock(item.getProductId(), - itemDto.getQuantity(), auth.getName());
        // Пересчет totalAmount
        BigDecimal totalAmount = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }

    public OrderDto deleteOrderItem(Long orderId, Long itemId, Authentication auth) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with id: " + orderId + " not found"));
        Long principalId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
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
        catalogClient.updateStock(item.getProductId(), item.getQuantity(), auth.getName());
        orderItemRepository.delete(item);
        BigDecimal totalAmount = orderItemRepository.findByOrderId(orderId).stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());
        return orderMapper.toDto(orderRepository.save(order));
    }
}
