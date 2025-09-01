package com.example.controller;


import com.example.dto.CategoryDto;
import com.example.dto.ProductDto;
import com.example.service.CatalogService;
import org.springframework.data.domain.Page;
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
    public Page<CategoryDto> getCategories(Pageable pageable, @RequestParam(required = false) String name) {
        return catalogService.getCategories(pageable, name);
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto dto) {
        return catalogService.createCategory(dto);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return catalogService.getCategory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/categories/{id}")
    public CategoryDto updateCategory(@PathVariable Long id, @RequestBody CategoryDto dto) {
        return catalogService.updateCategory(id, dto);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products")
    public Page<ProductDto> getProducts(Pageable pageable,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice,
                                        @RequestParam(required = false) Boolean onlyActive) {
        return catalogService.getProducts(pageable, categoryId, q, minPrice, maxPrice, onlyActive);
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto dto) {
        return catalogService.createProduct(dto);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return catalogService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/products/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto dto) {
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
