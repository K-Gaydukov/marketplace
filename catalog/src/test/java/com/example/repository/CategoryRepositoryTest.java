package com.example.repository;

import com.example.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(com.example.ApplicationCatalog.class)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findByName_shouldReturnCategory() {
        Category category = new Category();
        category.setName("Test");
        category.setDescription("Desc");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        Category found = categoryRepository.findByName("Test");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Test");
        assertThat(found.getDescription()).isEqualTo("Desc");
    }

    @Test
    void findAll_withSpecification_shouldReturnPage() {
        Category category = new Category();
        category.setName("Test");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        var spec = (Specification<Category>) (root, query, cb) -> cb.equal(root.get("name"), "Test");
        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        var page = categoryRepository.findAll(spec, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Test");
    }
}