package com.example.controller;

import com.example.dto.PageDto;
import com.example.dto.catalog.CategoryDto;
import com.example.dto.catalog.ProductDto;
import com.example.service.CatalogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
public class CatalogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogService catalogService;

    private CategoryDto categoryDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Test Category");
        categoryDto.setDescription("Description");
        categoryDto.setCreatedAt(LocalDateTime.now());
        categoryDto.setUpdatedAt(LocalDateTime.now());

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setSku("SKU123");
        productDto.setName("Test Product");
        productDto.setDescription("Product Description");
        productDto.setPrice(BigDecimal.valueOf(100));
        productDto.setStock(10);
        productDto.setActive(true);
        productDto.setCategoryId(1L);
        productDto.setCreatedAt(LocalDateTime.now());
        productDto.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getCategories_success() throws Exception {
        // Проверяет: GET /categories с пагинацией и фильтрацией
        PageDto<CategoryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(categoryDto));
        pageDto.setNumber(0);
        pageDto.setSize(10);
        pageDto.setTotalElements(1);
        when(catalogService.getCategories(any(Pageable.class), any())).thenReturn(pageDto);

        mockMvc.perform(get("/categories?page=0&size=10&name=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Category"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(catalogService).getCategories(any(Pageable.class), eq("Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_success() throws Exception {
        // Проверяет: POST /categories с валидным DTO
        when(catalogService.createCategory(any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(catalogService).createCategory(any(CategoryDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_forbidden() throws Exception {
        // Проверяет: POST /categories без роли ADMIN возвращает 403
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isForbidden());

        verify(catalogService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getCategory_success() throws Exception {
        // Проверяет: GET /categories/{id}
        when(catalogService.getCategory(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(catalogService).getCategory(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_success() throws Exception {
        // Проверяет: PUT /categories/{id}
        when(catalogService.updateCategory(eq(1L), any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category"));

        verify(catalogService).updateCategory(eq(1L), any(CategoryDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_success() throws Exception {
        // Проверяет: DELETE /categories/{id}
        doNothing().when(catalogService).deleteCategory(1L);

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNoContent());

        verify(catalogService).deleteCategory(1L);
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getProducts_success() throws Exception {
        // Проверяет: GET /products с фильтрацией
        PageDto<ProductDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(productDto));
        pageDto.setNumber(0);
        pageDto.setSize(10);
        pageDto.setTotalElements(1);
        when(catalogService.getProducts(any(Pageable.class), any(), any(), any(), any(), any())).thenReturn(pageDto);

        mockMvc.perform(get("/products?page=0&size=10&categoryId=1&q=Test&minPrice=50&maxPrice=150&onlyActive=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));

        verify(catalogService).getProducts(any(Pageable.class), eq(1L), eq("Test"), eq(BigDecimal.valueOf(50)), eq(BigDecimal.valueOf(150)), eq(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_success() throws Exception {
        // Проверяет: POST /products
        when(catalogService.createProduct(any(ProductDto.class))).thenReturn(productDto);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(catalogService).createProduct(any(ProductDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getProduct_success() throws Exception {
        // Проверяет: GET /products/{id}
        when(catalogService.getProduct(1L)).thenReturn(productDto);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(catalogService).getProduct(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_success() throws Exception {
        // Проверяет: PUT /products/{id}
        when(catalogService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(productDto);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(catalogService).updateProduct(eq(1L), any(ProductDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_success() throws Exception {
        // Проверяет: DELETE /products/{id}
        doNothing().when(catalogService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(catalogService).deleteProduct(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "SERVICE"})
    void updateStock_success() throws Exception {
        // Проверяет: PATCH /products/{id}/stock
        when(catalogService.updateStock(1L, 5)).thenReturn(productDto);

        mockMvc.perform(patch("/products/1/stock?delta=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));

        verify(catalogService).updateStock(1L, 5);
    }
}
