package com.example.client;

import com.example.dto.CategoryDto;
import com.example.dto.ProductDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class CatalogClient {

    private final RestTemplate restTemplate;
    @Value("${catalog.url")
    private String catalogUrl;

    public CatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Page<CategoryDto> getCategories(String token, int page, int size, String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/categories")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParamIfPresent("name", Optional.ofNullable(name))
                .toUriString();
        ResponseEntity<Page<CategoryDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Page<CategoryDto>>() {});
        return response.getBody();
    }

    public CategoryDto createCategory(String token, CategoryDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<CategoryDto> entity = new HttpEntity<>(dto, headers);
        return restTemplate.postForObject(catalogUrl + "/categories", entity, CategoryDto.class);
    }

    public ResponseEntity<CategoryDto> getCategory(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                catalogUrl + "/categories" + id, HttpMethod.GET, entity, CategoryDto.class);
    }

    public CategoryDto updateCategory(String token, Long id, CategoryDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<CategoryDto> entity = new HttpEntity<>(dto, headers);
        return restTemplate.exchange(
                catalogUrl + "/categories" + id, HttpMethod.PUT, entity, CategoryDto.class).getBody();
    }

    public void deleteCategory(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        restTemplate.exchange(catalogUrl + "/categories" + id, HttpMethod.DELETE, entity, Value.class);
    }

    public Page<ProductDto> getProducts(String token,
                                        int page,
                                        int size,
                                        Long categoryId,
                                        String q,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        Boolean onlyActive) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParamIfPresent("categoryId", Optional.ofNullable(categoryId))
                .queryParamIfPresent("q", Optional.ofNullable(q))
                .queryParamIfPresent("minPrice", Optional.ofNullable(minPrice))
                .queryParamIfPresent("maxPrice", Optional.ofNullable(maxPrice))
                .queryParamIfPresent("onlyActive", Optional.ofNullable(onlyActive))
                .toUriString();
        ResponseEntity<Page<ProductDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<Page<ProductDto>>() {});
        return response.getBody();
    }

    public ProductDto createProduct(String token, ProductDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<ProductDto> entity = new HttpEntity<>(dto, headers);
        return  restTemplate.postForObject(catalogUrl + "/products", entity, ProductDto.class);
    }

    public ResponseEntity<ProductDto> getProduct(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                catalogUrl + "/products" + id, HttpMethod.GET, entity, ProductDto.class);
    }

    public ProductDto updateProduct(String token, Long id, ProductDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<ProductDto> entity = new HttpEntity<>(dto, headers);
        return restTemplate.exchange(
                catalogUrl + "/products" + id, HttpMethod.PUT, entity, ProductDto.class).getBody();
    }

    public void deleteProduct(String token, Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        restTemplate.exchange(catalogUrl + "/products" + id, HttpMethod.DELETE, entity, Void.class);
    }

    public ProductDto updateStock(String token, Long id, Integer delta) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products" + id + "/stock")
                .queryParam("delta", delta)
                .toUriString();
        return restTemplate.exchange(url, HttpMethod.PATCH, entity, ProductDto.class).getBody();
    }
}
