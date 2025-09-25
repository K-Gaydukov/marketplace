package com.example.repository;

import com.example.entity.Category;
import com.example.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(com.example.ApplicationCatalog.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findBySku_shouldReturnProduct() {
        Category category = new Category();
        category.setName("TestCategory");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        Product product = new Product();
        product.setSku("SKU123");
        product.setName("Test");
        product.setPrice(BigDecimal.TEN);
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        Product found = productRepository.findBySku("SKU123");

        assertThat(found).isNotNull();
        assertThat(found.getSku()).isEqualTo("SKU123");
        assertThat(found.getName()).isEqualTo("Test");
    }

    @Test
    void findByCategoryId_shouldReturnList() {
        Category category = new Category();
        category.setName("TestCategory");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSku("SKU123");
        product.setName("book");
        product.setCategory(category);
        product.setPrice(BigDecimal.TEN);
        product.setStock(1);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        var found = productRepository.findByCategoryId(category.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getSku()).isEqualTo("SKU123");
    }

    @Test
    void findAll_withSpecification_shouldReturnPage() {
        Category category = new Category();
        category.setName("TestCategory");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        Product product = new Product();
        product.setSku("SKU123");
        product.setName("Test");
        product.setPrice(BigDecimal.TEN);
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        var spec = (Specification<Product>) (root, query, cb) -> cb.equal(root.get("sku"), "SKU123");
        var pageable = PageRequest.of(0, 10);
        Page<Product> page = productRepository.findAll(spec, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSku()).isEqualTo("SKU123");
    }
}
