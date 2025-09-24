package com.example.repository;

import com.example.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByName_success() {
        // Проверяет: Поиск категории по имени
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Description");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        Category found = categoryRepository.findByName("Test Category");

        assertNotNull(found);
        assertEquals("Test Category", found.getName());
    }

    @Test
    void findAll_withPagination() {
        // Проверяет: Получение списка категорий с пагинацией
        Category category = new Category();
        category.setName("Test Category");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        Page<Category> page = categoryRepository.findAll(PageRequest.of(0, 10));

        assertEquals(1, page.getContent().size());
        assertEquals("Test Category", page.getContent().get(0).getName());
    }
}
