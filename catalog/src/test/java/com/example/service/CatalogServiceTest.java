package com.example.service;

import com.example.dto.PageDto;
import com.example.dto.catalog.CategoryDto;
import com.example.dto.catalog.ProductDto;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.mapper.CategoryMapper;
import com.example.mapper.ProductMapper;
import com.example.repository.CategoryRepository;
import com.example.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CatalogService catalogService;

    private Category category;
    private Product product;
    private CategoryDto categoryDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        category.setDescription("Description");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        product = new Product();
        product.setId(1L);
        product.setSku("SKU123");
        product.setName("Test Product");
        product.setDescription("Product Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Test Category");
        categoryDto.setDescription("Description");

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setSku("SKU123");
        productDto.setName("Test Product");
        productDto.setDescription("Product Description");
        productDto.setPrice(BigDecimal.valueOf(100));
        productDto.setStock(10);
        productDto.setActive(true);
        productDto.setCategoryId(1L);
    }

    @Test
    void getCategories_success() {
        // Проверяет: Получение списка категорий с пагинацией и фильтрацией по имени
        // Как работает: Мокаем categoryRepository.findAll, возвращаем Page<Category>, проверяем преобразование в PageDto<CategoryDto>
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of(category), pageable, 1);
        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        PageDto<CategoryDto> result = catalogService.getCategories(pageable, "Test");

        assertEquals(1, result.getContent().size());
        assertEquals("Test Category", result.getContent().get(0).getName());
        assertEquals(0, result.getNumber());
        assertEquals(1, result.getTotalPages());
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
        verify(categoryMapper).toDto(category);
    }

    @Test
    void getCategories_empty() {
        // Проверяет: Возврат пустого списка категорий
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of(), pageable, 0);
        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageDto<CategoryDto> result = catalogService.getCategories(pageable, null);

        assertTrue(result.getContent().isEmpty());
        assertTrue(result.isEmpty());
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void createCategory_success() {
        // Проверяет: Создание новой категории
        // Как работает: Мокаем маппер и репозиторий, проверяем, что категория сохраняется и возвращается DTO
        when(categoryMapper.toEntity(categoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto result = catalogService.createCategory(categoryDto);

        assertEquals("Test Category", result.getName());
        assertNotNull(result.getCreatedAt());
        verify(categoryRepository).save(category);
        verify(categoryMapper).toEntity(categoryDto);
        verify(categoryMapper).toDto(category);
    }

    @Test
    void getCategory_success() {
        // Проверяет: Получение категории по ID
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto result = catalogService.getCategory(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test Category", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryMapper).toDto(category);
    }

    @Test
    void getCategory_notFound() {
        // Проверяет: Выброс NotFoundException при отсутствии категории
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> catalogService.getCategory(1L));
        verify(categoryRepository).findById(1L);
    }

    @Test
    void updateCategory_success() {
        // Проверяет: Обновление существующей категории
        CategoryDto updatedDto = new CategoryDto();
        updatedDto.setName("Updated Category");
        updatedDto.setDescription("Updated Description");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Category");
        updatedCategory.setDescription("Updated Description");
        updatedCategory.setCreatedAt(category.getCreatedAt());
        updatedCategory.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toEntity(updatedDto)).thenReturn(updatedCategory);
        when(categoryRepository.save(updatedCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toDto(updatedCategory)).thenReturn(updatedDto);

        CategoryDto result = catalogService.updateCategory(1L, updatedDto);

        assertEquals("Updated Category", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(updatedCategory);
        verify(categoryMapper).toDto(updatedCategory);
    }

    @Test
    void updateCategory_notFound() {
        // Проверяет: Выброс NotFoundException при обновлении несуществующей категории
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> catalogService.updateCategory(1L, categoryDto));
        verify(categoryRepository).findById(1L);
    }

    @Test
    void deleteCategory_success() {
        // Проверяет: Удаление категории по ID
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).deleteById(1L);

        catalogService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_notFound() {
        // Проверяет: Выброс NotFoundException при удалении несуществующей категории
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> catalogService.deleteCategory(1L));
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getProducts_success() {
        // Проверяет: Получение списка продуктов с фильтрацией
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(productMapper.toDto(product)).thenReturn(productDto);

        PageDto<ProductDto> result = catalogService.getProducts(pageable, 1L, "Test", BigDecimal.valueOf(50), BigDecimal.valueOf(150), true);

        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
        verify(productMapper).toDto(product);
    }

    @Test
    void createProduct_success() {
        // Проверяет: Создание продукта с валидной категорией
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(productDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);

        ProductDto result = catalogService.createProduct(productDto);

        assertEquals("Test Product", result.getName());
        assertEquals(1L, result.getCategoryId());
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(product);
        verify(productMapper).toEntity(productDto);
        verify(productMapper).toDto(product);
    }

    @Test
    void createProduct_categoryNotFound() {
        // Проверяет: Выброс NotFoundException при создании продукта с несуществующей категорией
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> catalogService.createProduct(productDto));
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getProduct_success() {
        // Проверяет: Получение продукта по ID
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto);

        ProductDto result = catalogService.getProduct(1L);

        assertEquals("Test Product", result.getName());
        verify(productRepository).findById(1L);
        verify(productMapper).toDto(product);
    }

    @Test
    void getProduct_notFound() {
        // Проверяет: Выброс NotFoundException при отсутствии продукта
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> catalogService.getProduct(1L));
        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_success() {
        // Проверяет: Обновление продукта с изменением всех полей
        ProductDto updatedDto = new ProductDto();
        updatedDto.setSku("SKU456");
        updatedDto.setName("Updated Product");
        updatedDto.setDescription("Updated Description");
        updatedDto.setPrice(BigDecimal.valueOf(200));
        updatedDto.setStock(20);
        updatedDto.setActive(false);
        updatedDto.setCategoryId(1L);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setSku("SKU456");
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(BigDecimal.valueOf(200));
        updatedProduct.setStock(20);
        updatedProduct.setActive(false);
        updatedProduct.setCategory(category);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(updatedProduct);
        when(productMapper.toDto(updatedProduct)).thenReturn(updatedDto);

        ProductDto result = catalogService.updateProduct(1L, updatedDto);

        assertEquals("Updated Product", result.getName());
        assertEquals("SKU456", result.getSku());
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(product);
        verify(productMapper).toDto(updatedProduct);
    }

    @Test
    void updateProduct_notFound() {
        // Проверяет: Выброс NotFoundException при обновлении несуществующего продукта
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> catalogService.updateProduct(1L, productDto));
        verify(productRepository).findById(1L);
    }

    @Test
    void deleteProduct_success() {
        // Проверяет: Удаление продукта по ID
        doNothing().when(productRepository).deleteById(1L);

        catalogService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void updateStock_success() {
        // Проверяет: Обновление запаса продукта
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);

        ProductDto result = catalogService.updateStock(1L, 5);

        assertEquals(15, result.getStock());
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
    }

    @Test
    void updateStock_negativeStock_throwsException() {
        // Проверяет: Выброс ValidationException при попытке сделать отрицательный запас
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ValidationException.class, () -> catalogService.updateStock(1L, -20));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any()); // Убедимся, что save не вызывается
    }
}
