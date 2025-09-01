package com.example.mapper;

import com.example.dto.ProductDto;
import com.example.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryId", source = "category.getId")
    ProductDto toDto(Product product);
    Product toEntity(ProductDto dto);
}
