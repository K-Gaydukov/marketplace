package com.example.controller;


import com.example.dto.catalog.CategoryDto;
import com.example.dto.PageDto;
import com.example.dto.catalog.ProductDto;
import com.example.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public PageDto<CategoryDto> getCategories(Pageable pageable, @RequestParam(required = false) String name) {
        return catalogService.getCategories(pageable, name);
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(@Valid @RequestBody CategoryDto dto) {
        return catalogService.createCategory(dto);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return catalogService.getCategory(id);
    }

    @PutMapping("/categories/{id}")
    public CategoryDto updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return catalogService.updateCategory(id, dto);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products")
    public PageDto<ProductDto> getProducts(Pageable pageable,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(required = false) BigDecimal minPrice,
                                           @RequestParam(required = false) BigDecimal maxPrice,
                                           @RequestParam(required = false) Boolean onlyActive) {
        return catalogService.getProducts(pageable, categoryId, q, minPrice, maxPrice, onlyActive);
    }

    @PostMapping("/products")
    public ProductDto createProduct(@Valid @RequestBody ProductDto dto) {
        return catalogService.createProduct(dto);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return catalogService.getProduct(id);
    }

    @PutMapping("/products/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        return catalogService.updateProduct(id, dto);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/products/{id}/stock")
    public ProductDto updateStock(@PathVariable Long id, @RequestParam Integer delta) {
        return catalogService.updateStock(id, delta);
    }
}
