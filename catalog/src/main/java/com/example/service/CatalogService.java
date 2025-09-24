package com.example.service;

import com.example.dto.catalog.CategoryDto;
import com.example.dto.PageDto;
import com.example.dto.catalog.ProductDto;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.mapper.CategoryMapper;
import com.example.mapper.ProductMapper;
import com.example.repository.CategoryRepository;
import com.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Transactional
public class CatalogService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CatalogService(CategoryRepository categoryRepository, ProductRepository productRepository,
                          CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PageDto<CategoryDto> getCategories(Pageable pageable, String name) {
        Specification<Category> spec = Specification.where(null);
        if (name != null) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase(Locale.forLanguageTag("ru")) + "%"));
        }
        Page<Category> page = categoryRepository.findAll(spec, pageable);
        return new PageDto<>(page.map(categoryMapper::toDto));
    }

    public CategoryDto createCategory(CategoryDto dto) {
        Category category = categoryMapper.toEntity(dto);

        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id " + id + " not found"));
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        category.setUpdatedAt(LocalDateTime.now());
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PageDto<ProductDto> getProducts(Pageable pageable,
                                        Long categoryId,
                                        String q,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        Boolean onlyActive) {
        Specification<Product> spec = Specification.where(null);
        if (categoryId != null) spec = spec.and((root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId));
        if (q != null) spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"));
        if (minPrice != null) spec = spec.and((root, query, cb) ->
                cb.ge(root.get("price"), minPrice));
        if (maxPrice != null) spec = spec.and((root, query, cb) ->
                cb.le(root.get("price"), maxPrice));
        if (onlyActive != null) spec = spec.and((root, query, cb) ->
                cb.equal(root.get("isActive"), onlyActive));
        Page<Product> page = productRepository.findAll(spec, pageable);
        return new PageDto<>(page.map(productMapper::toDto));
    }

    public ProductDto createProduct(ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category with id " + dto.getCategoryId() + " not found")));
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));
    }

    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));
        if (dto.getSku() != null) {
            product.setSku(dto.getSku());
        }
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        product.setActive(dto.isActive());
        if (dto.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category with id " + dto.getCategoryId() + " not found")));
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productMapper.toDto(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public ProductDto updateStock(Long id, Integer delta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id " + id + " not found"));
        product.setStock(product.getStock() + delta);
        if (product.getStock() < 0) {
            throw new ValidationException("Stock cannot be negative");
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productMapper.toDto(productRepository.save(product));
    }
}
