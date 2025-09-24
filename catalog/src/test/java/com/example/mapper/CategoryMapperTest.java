package com.example.mapper;

import com.example.dto.catalog.CategoryDto;
import com.example.entity.Category;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CategoryMapperTest {
    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toDto_success() {
        // Проверяет: Маппинг Category в CategoryDto
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setDescription("Description");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        CategoryDto dto = categoryMapper.toDto(category);

        assertEquals(1L, dto.getId());
        assertEquals("Test Category", dto.getName());
        assertEquals("Description", dto.getDescription());
    }

    @Test
    void toEntity_success() {
        // Проверяет: Маппинг CategoryDto в Category
        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Test Category");
        dto.setDescription("Description");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        Category category = categoryMapper.toEntity(dto);

        assertEquals("Test Category", category.getName());
        assertEquals("Description", category.getDescription());
    }
}
