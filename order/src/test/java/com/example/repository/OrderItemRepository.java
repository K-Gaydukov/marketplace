package com.example.repository;

import com.example.ApplicationOrder;
import com.example.entity.Order;
import com.example.entity.OrderItem;
import com.example.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(ApplicationOrder.class)
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByOrderId_shouldReturnItems() {
        Order order = new Order();
        order.setUserId(1L);
        order.setUserFio("test");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(1L);
        item.setProductName("Test product");
        item.setQuantity(2);
        item.setProductPrice(BigDecimal.TEN);
        item.setLineTotal(BigDecimal.valueOf(20));
        orderItemRepository.save(item);

        List<OrderItem> result = orderItemRepository.findByOrderId(order.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void deleteByOrderId_shouldDeleteItems() {
        Order order = new Order();
        order.setUserId(1L);
        order.setUserFio("test");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(1L);
        item.setProductName("Test product");
        item.setQuantity(2);
        item.setProductPrice(BigDecimal.TEN);
        item.setLineTotal(BigDecimal.valueOf(20));
        orderItemRepository.save(item);

        orderItemRepository.deleteByOrderId(order.getId());

        List<OrderItem> result = orderItemRepository.findByOrderId(order.getId());
        assertThat(result).isEmpty();
    }
}