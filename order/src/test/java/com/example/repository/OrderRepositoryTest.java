package com.example.repository;

import com.example.ApplicationOrder;
import com.example.entity.Order;
import com.example.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(ApplicationOrder.class) // Замените на ваш главный класс приложения
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findByUserId_shouldReturnOrders() {
        Order order = new Order();
        order.setUserId(1L);
        order.setUserFio("test");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        Page<Order> result = orderRepository.findByUserId(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void findByStatus_shouldReturnOrders() {
        Order order = new Order();
        order.setUserId(1L);
        order.setUserFio("test");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        Page<Order> result = orderRepository.findByStatus(OrderStatus.NEW, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void findByUserIdAndStatus_shouldReturnOrders() {
        Order order = new Order();
        order.setUserId(1L);
        order.setUserFio("test");
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(BigDecimal.TEN);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        Page<Order> result = orderRepository.findByUserIdAndStatus(1L, OrderStatus.NEW, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.NEW);
    }
}
