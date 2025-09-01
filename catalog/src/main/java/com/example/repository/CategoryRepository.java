package com.example.repository;

import com.example.dto.CategoryDto;
import com.example.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);

    Page<Category> findAll(Specification<Category> spec, Pageable pageable);
}
