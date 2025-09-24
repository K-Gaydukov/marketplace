package com.example.repository;

import com.example.entity.Category;
import com.example.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findBySku_success() {
        // Проверяет: Поиск продукта по SKU
        Category category = new Category();
        category.setName("Test Category");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSku("SKU123");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        Product found = productRepository.findBySku("SKU123");

        assertNotNull(found);
        assertEquals("SKU123", found.getSku());
    }

    @Test
    void findByCategoryId_success() {
        // Проверяет: Поиск продуктов по ID категории
        Category category = new Category();
        category.setName("Test Category");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSku("SKU123");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        List<Product> found = productRepository.findByCategoryId(category.getId());

        assertEquals(1, found.size());
        assertEquals("Test Product", found.get(0).getName());
    }

    @Test
    void findAll_withPagination() {
        // Проверяет: Получение списка продуктов с пагинацией
        Category category = new Category();
        category.setName("Test Category");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSku("SKU123");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        Page<Product> page = productRepository.findAll(PageRequest.of(0, 10));

        assertEquals(1, page.getContent().size());
        assertEquals("Test Product", page.getContent().get(0).getName());
    }
}
