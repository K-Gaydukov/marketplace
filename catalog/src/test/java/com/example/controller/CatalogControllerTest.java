package com.example.controller;

import com.example.config.SecurityConfig;
import com.example.dto.PageDto;
import com.example.dto.catalog.CategoryDto;
import com.example.dto.catalog.ProductDto;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.service.CatalogService;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@Import(SecurityConfig.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Claims userClaims;
    private Claims adminClaims;

    @BeforeEach
    void setUp() {
        // Создаём Claims через Jwts.claims() (из jjwt-api)
        userClaims = Jwts.claims()
                .setSubject("test-user")
                .add("role", "ROLE_USER")
                .add("uid", 1L)
                .add("fio", "Test User").build();

        adminClaims = Jwts.claims()
                .setSubject("test-admin")
                .add("role", "ROLE_ADMIN")
                .add("uid", 2L)
                .add("fio", "Test Admin").build();
    }

    @Test
    void getCategories_shouldReturn200() throws Exception {
        PageDto<CategoryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new CategoryDto()));
        when(catalogService.getCategories(any(), eq(null))).thenReturn(pageDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getCategories_shouldReturn200_withName() throws Exception {
        PageDto<CategoryDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new CategoryDto()));
        when(catalogService.getCategories(any(), eq("Test"))).thenReturn(pageDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("name", "Test")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createCategory_shouldReturn201() throws Exception {
        CategoryDto dto = new CategoryDto();
        dto.setName("Test");
        dto.setDescription("Desc");
        when(catalogService.createCategory(any())).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.description").value("Desc"));
    }

    @Test
    void createCategory_shouldReturn422_whenInvalid() throws Exception {
        CategoryDto dto = new CategoryDto(); // Пустое имя вызовет ValidationException
        when(catalogService.createCategory(any())).thenThrow(new ValidationException("Name is required"));
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Name is required"));
    }

    @Test
    void getCategory_shouldReturn200_whenFound() throws Exception {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        dto.setId(id);
        dto.setName("Test");
        when(catalogService.getCategory(id)).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/categories/{id}", id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void getCategory_shouldReturn404_whenNotFound() throws Exception {
        Long id = 1L;
        when(catalogService.getCategory(id)).thenThrow(new NotFoundException("Category with id " + id + " not found"));
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/categories/{id}", id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category with id " + id + " not found"));
    }

    @Test
    void updateCategory_shouldReturn200() throws Exception {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");
        when(catalogService.updateCategory(eq(id), any())).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(put("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateCategory_shouldReturn404_whenNotFound() throws Exception {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        dto.setName("Book");
        when(catalogService.updateCategory(eq(id), any())).thenThrow(new NotFoundException("Category with id " + id + " not found"));
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(put("/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category with id " + id + " not found"));
    }

    @Test
    void deleteCategory_shouldReturn204() throws Exception {
        Long id = 1L;
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(delete("/categories/{id}", id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getProducts_shouldReturn200() throws Exception {
        PageDto<ProductDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new ProductDto()));
        when(catalogService.getProducts(any(), eq(null), eq(null), eq(null), eq(null), eq(null))).thenReturn(pageDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getProducts_shouldReturn200_withFilters() throws Exception {
        PageDto<ProductDto> pageDto = new PageDto<>();
        pageDto.setContent(List.of(new ProductDto()));
        when(catalogService.getProducts(any(), eq(1L), eq("search"), eq(BigDecimal.valueOf(1.0)), eq(BigDecimal.valueOf(10.0)), eq(true))).thenReturn(pageDto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", "1")
                        .param("q", "search")
                        .param("minPrice", "1.0")
                        .param("maxPrice", "10.0")
                        .param("onlyActive", "true")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void createProduct_shouldReturn201() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setSku("SKU123");
        dto.setName("Product");
        dto.setDescription("Desc");
        dto.setPrice(BigDecimal.TEN);
        dto.setStock(100);
        dto.setActive(true);
        dto.setCategoryId(1L);
        when(catalogService.createProduct(any())).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU123"));
    }

    @Test
    void createProduct_shouldReturn422_whenInvalid() throws Exception {
        ProductDto dto = new ProductDto(); // Пустой DTO вызовет ValidationException
        when(catalogService.createProduct(any())).thenThrow(new ValidationException("Category ID cannot be empty, SKU cannot be empty, Name cannot be empty"));
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getProduct_shouldReturn200_whenFound() throws Exception {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        dto.setId(id);
        dto.setSku("SKU123");
        when(catalogService.getProduct(id)).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/products/{id}", id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.sku").value("SKU123"));
    }

    @Test
    void getProduct_shouldReturn404_whenNotFound() throws Exception {
        Long id = 1L;
        when(catalogService.getProduct(id)).thenThrow(new NotFoundException("Product with id " + id + " not found"));
        when(jwtUtil.validateToken("test-token")).thenReturn(userClaims);

        mockMvc.perform(get("/products/{id}", id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id " + id + " not found"));
    }

    @Test
    void updateProduct_shouldReturn200() throws Exception {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        dto.setName("Updated");
        dto.setCategoryId(1L);
        dto.setSku("BN123");
        dto.setActive(false);
        when(catalogService.updateProduct(eq(id), any())).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateProduct_shouldReturn404_whenNotFound() throws Exception {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        dto.setName("Updated");
        dto.setCategoryId(1L);
        dto.setSku("BN123");
        dto.setActive(false);
        when(catalogService.updateProduct(eq(id), any())).thenThrow(new NotFoundException("Product with id " + id + " not found"));
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id " + id + " not found"));
    }

    @Test
    void deleteProduct_shouldReturn204() throws Exception {
        Long id = 1L;
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(delete("/products/{id}", id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStock_shouldReturn200() throws Exception {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        when(catalogService.updateStock(id, 5)).thenReturn(dto);
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(patch("/products/{id}/stock", id)
                        .param("delta", "5")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStock_shouldReturn422_whenInvalid() throws Exception {
        Long id = 1L;
        when(catalogService.updateStock(id, -15)).thenThrow(new ValidationException("Stock cannot be negative"));
        when(jwtUtil.validateToken("test-token")).thenReturn(adminClaims);

        mockMvc.perform(patch("/products/{id}/stock", id)
                        .param("delta", "-15")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Stock cannot be negative"));
    }
}
