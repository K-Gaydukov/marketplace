package com.example.mapper;

import com.example.dto.catalog.ProductDto;
import com.example.entity.Category;
import com.example.entity.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class ProductMapperTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toDto_success() {
        // Проверяет: Маппинг Product в ProductDto
        Category category = new Category();
        category.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setSku("SKU123");
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        ProductDto dto = productMapper.toDto(product);

        assertEquals(1L, dto.getId());
        assertEquals("SKU123", dto.getSku());
        assertEquals("Test Product", dto.getName());
        assertEquals(1L, dto.getCategoryId());
    }

    @Test
    void toEntity_success() {
        // Проверяет: Маппинг ProductDto в Product
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setSku("SKU123");
        dto.setName("Test Product");
        dto.setDescription("Description");
        dto.setPrice(BigDecimal.valueOf(100));
        dto.setStock(10);
        dto.setActive(true);
        dto.setCategoryId(1L);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        Product product = productMapper.toEntity(dto);

        assertEquals("SKU123", product.getSku());
        assertEquals("Test Product", product.getName());
        assertNull(product.getCategory()); // Category не маппится, так как toEntity не устанавливает связь
    }
}
