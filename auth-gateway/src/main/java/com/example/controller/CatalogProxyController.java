package com.example.controller;

import com.example.client.CatalogClient;
import com.example.dto.CategoryDto;
import com.example.dto.PageDto;
import com.example.dto.ProductDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/catalog")
public class CatalogProxyController {

    private final CatalogClient catalogClient;

    public CatalogProxyController(CatalogClient client) {
        this.catalogClient = client;
    }

    private String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
    }

    @GetMapping("/categories")
    public PageDto<CategoryDto> getCategories(HttpServletRequest request,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(required = false) String name) {
        return catalogClient.getCategories(getToken(request), page, size, name);
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(HttpServletRequest request,
                                      @RequestBody CategoryDto dto) {
        return catalogClient.createCategory(getToken(request), dto);
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> getCategory(HttpServletRequest request,
                                                   @PathVariable Long id) {
        return catalogClient.getCategory(getToken(request), id);
    }

    @PutMapping("/categories/{id}")
    public CategoryDto updateCategory(HttpServletRequest request,
                                      @PathVariable Long id,
                                      @RequestBody CategoryDto dto) {
        return catalogClient.updateCategory(getToken(request), id, dto);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(HttpServletRequest request,
                                               @PathVariable Long id) {
        catalogClient.deleteCategory(getToken(request), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products")
    public PageDto<ProductDto> getProducts(HttpServletRequest request,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice,
                                        @RequestParam(required = false) Boolean onlyActive) {
        return catalogClient.getProducts(
                getToken(request), page, size, categoryId, q, minPrice, maxPrice, onlyActive);
    }

    @PostMapping("/products")
    public ProductDto createProduct(HttpServletRequest request,
                                    @RequestBody ProductDto dto) {
        return catalogClient.createProduct(getToken(request), dto);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProduct(HttpServletRequest request,
                                                 @PathVariable Long id) {
        return catalogClient.getProduct(getToken(request), id);
    }

    @PutMapping("/products/{id}")
    public  ProductDto updateProduct(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestBody ProductDto dto) {
        return catalogClient.updateProduct(getToken(request), id, dto);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(HttpServletRequest request,
                                              @PathVariable Long id) {
        catalogClient.deleteProduct(getToken(request), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/products/{id}")
    public ProductDto updateStock(HttpServletRequest request,
                                  @PathVariable Long id,
                                  @RequestParam Integer delta) {
        return catalogClient.updateStock(getToken(request), id, delta);
    }
}
