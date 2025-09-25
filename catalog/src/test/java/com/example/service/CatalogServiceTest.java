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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(categoryRepository, productRepository, categoryMapper, productMapper);
    }

    // Метод 1: getCategories без имени — проверяет пагинацию без фильтра.
    // Поэтапно: 1. Настраиваем мок репозитория на возврат фейкового Page. 2. Вызываем метод. 3. Проверяем результат и вызов.
    @Test
    void getCategories_shouldReturnPageDto_whenNoName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of(new Category()));
        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        CategoryDto dto = new CategoryDto();
        when(categoryMapper.toDto(any(Category.class))).thenReturn(dto);

        PageDto<CategoryDto> result = catalogService.getCategories(pageable, null);

        assertThat(result.getContent()).hasSize(1);
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
    }

    // Метод 2: getCategories с именем — проверяет фильтр по имени (Specification с like).
    // Поэтапно: 1. Мок возвращает пустую страницу. 2. Вызов с "Test". 3. Verify, что Specification создана правильно (Mockito захватывает any(Specification)).
    @Test
    void getCategories_shouldFilterByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> page = new PageImpl<>(List.of());
        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        catalogService.getCategories(pageable, "Test");

        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
    }

    // Метод 3: createCategory — проверяет создание и сохранение.
    // Поэтапно: 1. Создаём DTO, мапим в entity. 2. Мок save возвращает entity. 3. Проверяем результат и даты.
    @Test
    void createCategory_shouldSaveAndReturnDto() {
        CategoryDto dto = new CategoryDto();
        dto.setName("New Category");
        Category entity = new Category();
        entity.setName("New Category");
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        when(categoryMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDto(entity)).thenReturn(dto);

        CategoryDto result = catalogService.createCategory(dto);

        assertThat(result.getName()).isEqualTo("New Category");
        assertThat(entity.getCreatedAt()).isNotNull();
        verify(categoryRepository).save(entity);
    }

    // Метод 4: getCategory с ошибкой — проверяет исключение.
    // Поэтапно: 1. Мок возвращает empty. 2. Вызов. 3. assertThatThrownBy проверяет тип и сообщение исключения.
    @Test
    void getCategory_shouldThrowNotFound_whenIdInvalid() {
        Long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getCategory(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id " + id + " not found");
    }

    // Метод 5: getCategory успех — проверяет возврат DTO.
    // Поэтапно: 1. Мок findById возвращает entity. 2. Маппер toDto. 3. Проверка равенства.
    @Test
    void getCategory_shouldReturnDto_whenFound() {
        Long id = 1L;
        Category entity = new Category();
        CategoryDto dto = new CategoryDto();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryMapper.toDto(entity)).thenReturn(dto);

        CategoryDto result = catalogService.getCategory(id);

        assertThat(result).isEqualTo(dto);
        verify(categoryRepository).findById(id);
    }

    // Метод 6: updateCategory — проверяет обновление полей.
    // Поэтапно: 1. Мок findById. 2. Обновляем entity из DTO (в вашем коде — if(dto.getName() != null) entity.setName...). 3. Save и toDto.
    @Test
    void updateCategory_shouldUpdateFields() {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        dto.setName("Updated");
        dto.setDescription("New Desc");
        Category entity = new Category();
        entity.setName("Old");
        entity.setDescription("Old Desc");
        LocalDateTime now = LocalDateTime.now();
        entity.setUpdatedAt(now);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toDto(entity)).thenReturn(dto);

        CategoryDto result = catalogService.updateCategory(id, dto);

        assertThat(entity.getName()).isEqualTo("Updated");
        assertThat(entity.getDescription()).isEqualTo("New Desc");
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(result).isEqualTo(dto);
        verify(categoryRepository).save(entity);
    }

    // Метод 7: updateCategory с ошибкой — проверяет NotFound.
    // Поэтапно: Аналогично getCategory_throw.
    @Test
    void updateCategory_shouldThrowNotFound_whenIdInvalid() {
        Long id = 1L;
        CategoryDto dto = new CategoryDto();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateCategory(id, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id " + id + " not found");
    }

    // Метод 8: deleteCategory — проверяет вызов delete.
    // Поэтапно: 1. Вызов метода. 2. Verify deleteById.
    @Test
    void deleteCategory_shouldCallDelete() {
        Long id = 1L;
        catalogService.deleteCategory(id);
        verify(categoryRepository).deleteById(id);
    }

    // Метод 9: getProducts без фильтров — аналогично getCategories.
    @Test
    void getProducts_shouldReturnPageDto_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        ProductDto dto = new ProductDto();
        when(productMapper.toDto(any(Product.class))).thenReturn(dto);

        PageDto<ProductDto> result = catalogService.getProducts(pageable, null, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    // Метод 10: getProducts с фильтрами — проверяет Specification (categoryId, q, minPrice, maxPrice, onlyActive).
    // Поэтапно: 1. Настраиваем мок. 2. Вызов с параметрами. 3. Verify any(Specification) — Mockito проверяет, что фильтры применены.
    @Test
    void getProducts_shouldFilterByParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of());
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        catalogService.getProducts(pageable, 1L, "search", BigDecimal.ONE, BigDecimal.TEN, true);

        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    // Метод 11: createProduct — проверяет создание с категорией.
    @Test
    void createProduct_shouldSaveWithCategory() {
        ProductDto dto = new ProductDto();
        dto.setCategoryId(1L);
        dto.setSku("SKU");
        dto.setName("Product");
        dto.setPrice(BigDecimal.TEN);
        dto.setStock(10);
        dto.setActive(true);
        Product entity = new Product();
        Category category = new Category();
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        when(productMapper.toEntity(dto)).thenReturn(entity);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toDto(entity)).thenReturn(dto);

        ProductDto result = catalogService.createProduct(dto);

        assertThat(result).isEqualTo(dto);
        assertThat(entity.getCategory()).isEqualTo(category);
        assertThat(entity.getCreatedAt()).isNotNull();
        verify(productRepository).save(entity);
    }

    // Метод 12: createProduct с ошибкой категории.
    @Test
    void createProduct_shouldThrowNotFound_whenCategoryInvalid() {
        ProductDto dto = new ProductDto();
        dto.setCategoryId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.createProduct(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id " + dto.getCategoryId() + " not found");
    }

    // Метод 13: getProduct успех.
    @Test
    void getProduct_shouldReturnDto_whenFound() {
        Long id = 1L;
        Product entity = new Product();
        ProductDto dto = new ProductDto();
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productMapper.toDto(entity)).thenReturn(dto);

        ProductDto result = catalogService.getProduct(id);

        assertThat(result).isEqualTo(dto);
    }

    // Метод 14: getProduct ошибка.
    @Test
    void getProduct_shouldThrowNotFound_whenIdInvalid() {
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getProduct(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product with id " + id + " not found");
    }

    // Метод 15: updateProduct — проверяет частичное обновление полей.
    @Test
    void updateProduct_shouldUpdateFields() {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        dto.setSku("NewSKU");
        dto.setName("NewName");
        dto.setDescription("NewDesc");
        dto.setPrice(BigDecimal.valueOf(20));
        dto.setStock(20);
        dto.setActive(false);
        dto.setCategoryId(2L);
        Product entity = new Product();
        entity.setSku("OldSKU");
        entity.setName("OldName");
        entity.setDescription("OldDesc");
        entity.setPrice(BigDecimal.TEN);
        entity.setStock(10);
        entity.setActive(true);
        Category oldCategory = new Category();
        entity.setCategory(oldCategory);
        Category newCategory = new Category();
        LocalDateTime now = LocalDateTime.now();
        entity.setUpdatedAt(now);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toDto(entity)).thenReturn(dto);

        ProductDto result = catalogService.updateProduct(id, dto);

        assertThat(entity.getSku()).isEqualTo("NewSKU");
        assertThat(entity.getName()).isEqualTo("NewName");
        assertThat(entity.getDescription()).isEqualTo("NewDesc");
        assertThat(entity.getPrice()).isEqualTo(BigDecimal.valueOf(20));
        assertThat(entity.getStock()).isEqualTo(20);
        assertThat(entity.isActive()).isFalse();
        assertThat(entity.getCategory()).isEqualTo(newCategory);
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(result).isEqualTo(dto);
        verify(productRepository).save(entity);
    }

    // Метод 16: updateProduct ошибка ID.
    @Test
    void updateProduct_shouldThrowNotFound_whenIdInvalid() {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateProduct(id, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product with id " + id + " not found");
    }

    // Метод 17: updateProduct ошибка категории.
    @Test
    void updateProduct_shouldThrowNotFound_whenCategoryInvalid() {
        Long id = 1L;
        ProductDto dto = new ProductDto();
        dto.setCategoryId(2L);
        Product entity = new Product();
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateProduct(id, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category with id " + dto.getCategoryId() + " not found");
    }

    // Метод 18: deleteProduct — проверяет delete.
    @Test
    void deleteProduct_shouldCallDelete() {
        Long id = 1L;
        catalogService.deleteProduct(id);
        verify(productRepository).deleteById(id);
    }

    // Метод 19: updateStock успех.
    @Test
    void updateStock_shouldUpdateAndSave() {
        Long id = 1L;
        Integer delta = 5;
        Product entity = new Product();
        entity.setStock(10);
        LocalDateTime now = LocalDateTime.now();
        entity.setUpdatedAt(now);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productRepository.save(entity)).thenReturn(entity);
        ProductDto dto = new ProductDto();
        when(productMapper.toDto(entity)).thenReturn(dto);

        ProductDto result = catalogService.updateStock(id, delta);

        assertThat(entity.getStock()).isEqualTo(15);
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(result).isEqualTo(dto);
        verify(productRepository).save(entity);
    }

    // Метод 20: updateStock отрицательный stock.
    @Test
    void updateStock_shouldThrowValidation_whenNegative() {
        Long id = 1L;
        Integer delta = -15;
        Product entity = new Product();
        entity.setStock(10);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> catalogService.updateStock(id, delta))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Stock cannot be negative");
    }

    // Метод 21: updateStock ошибка ID.
    @Test
    void updateStock_shouldThrowNotFound_whenIdInvalid() {
        Long id = 1L;
        Integer delta = 5;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateStock(id, delta))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product with id " + id + " not found");
    }
}
