package com.example.repository;

import com.example.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findBySku(String sku);
    List<Product> findByCategoryId(Long categoryId);
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
