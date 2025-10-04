package com.example.client;

import com.example.dto.PageDto;
import com.example.dto.catalog.CategoryDto;
import com.example.dto.catalog.ProductDto;
import com.example.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "catalog.url=http://localhost:8081"
})
class CatalogClientTest {

    @Autowired
    private CatalogClient catalogClient;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RestTemplate restTemplate;

    private String token;
    private CategoryDto categoryDto;
    private ProductDto productDto;
    private PageDto<CategoryDto> categoryPageDto;
    private PageDto<ProductDto> productPageDto;
    private String catalogUrl;

    @BeforeEach
    void setUp() {
        token = "test-token";
        catalogUrl = "http://localhost:8081";

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Test Category");

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Test Product");
        productDto.setPrice(BigDecimal.valueOf(100.0));
        productDto.setStock(10);
        productDto.setActive(true);

//        categoryPageDto = new PageDto<>();
//        categoryPageDto.setContent(List.of(categoryDto));
//        categoryPageDto.setPage(0);
//        categoryPageDto.setSize(10);
//        categoryPageDto.setTotalElements(1);
//
//        productPageDto = new PageDto<>();
//        productPageDto.setContent(List.of(productDto));
//        productPageDto.setPage(0);
//        productPageDto.setSize(10);
//        productPageDto.setTotalElements(1);
    }

    // getCategories
    @Test
    void getCategories_shouldReturnPagedCategories() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/categories")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("name", "Test")
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<CategoryDto>>() {})))
                .thenReturn(new ResponseEntity<>(categoryPageDto, HttpStatus.OK));

        PageDto<CategoryDto> result = catalogClient.getCategories(token, 0, 10, "Test");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Category", result.getContent().get(0).getName());
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<CategoryDto>>() {}));
        verifyAuthorizationHeader();
    }

    @Test
    void getCategories_shouldHandleNullName() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/categories")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<CategoryDto>>() {})))
                .thenReturn(new ResponseEntity<>(categoryPageDto, HttpStatus.OK));

        PageDto<CategoryDto> result = catalogClient.getCategories(token, 0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<CategoryDto>>() {}));
        verifyAuthorizationHeader();
    }

    @Test
    void getCategories_shouldThrowHttpClientErrorException() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/categories")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<CategoryDto>>() {})))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.getCategories(token, 0, 10, null));
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<CategoryDto>>() {}));
        verifyAuthorizationHeader();
    }

    // createCategory
    @Test
    void createCategory_shouldCreateCategory() {
        when(restTemplate.postForObject(eq(catalogUrl + "/categories"), any(HttpEntity.class), eq(CategoryDto.class)))
                .thenReturn(categoryDto);

        CategoryDto result = catalogClient.createCategory(token, categoryDto);

        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        verify(restTemplate).postForObject(eq(catalogUrl + "/categories"), any(HttpEntity.class), eq(CategoryDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void createCategory_shouldThrowHttpClientErrorException() {
        when(restTemplate.postForObject(eq(catalogUrl + "/categories"), any(HttpEntity.class), eq(CategoryDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.createCategory(token, categoryDto));
        verify(restTemplate).postForObject(eq(catalogUrl + "/categories"), any(HttpEntity.class), eq(CategoryDto.class));
        verifyAuthorizationHeader();
    }

    // getCategory
    @Test
    void getCategory_shouldReturnCategory() {
        when(restTemplate.exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(CategoryDto.class)))
                .thenReturn(new ResponseEntity<>(categoryDto, HttpStatus.OK));

        ResponseEntity<CategoryDto> result = catalogClient.getCategory(token, 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Test Category", result.getBody().getName());
        verify(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(CategoryDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void getCategory_shouldThrowHttpClientErrorException() {
        when(restTemplate.exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(CategoryDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.getCategory(token, 1L));
        verify(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(CategoryDto.class));
        verifyAuthorizationHeader();
    }

    // updateCategory
    @Test
    void updateCategory_shouldUpdateCategory() {
        when(restTemplate.exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(CategoryDto.class)))
                .thenReturn(new ResponseEntity<>(categoryDto, HttpStatus.OK));

        CategoryDto result = catalogClient.updateCategory(token, 1L, categoryDto);

        assertNotNull(result);
        assertEquals("Test Category", result.getName());
        verify(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(CategoryDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void updateCategory_shouldThrowHttpClientErrorException() {
        when(restTemplate.exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(CategoryDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.updateCategory(token, 1L, categoryDto));
        verify(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(CategoryDto.class));
        verifyAuthorizationHeader();
    }

    // deleteCategory
    @Test
    void deleteCategory_shouldDeleteCategory() {
        doNothing().when(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));

        catalogClient.deleteCategory(token, 1L);

        verify(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verifyAuthorizationHeader();
    }

    @Test
    void deleteCategory_shouldThrowHttpClientErrorException() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.deleteCategory(token, 1L));
        verify(restTemplate).exchange(eq(catalogUrl + "/categories/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verifyAuthorizationHeader();
    }

    // getProducts
    @Test
    void getProducts_shouldReturnPagedProducts() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("categoryId", 1L)
                .queryParam("q", "Test")
                .queryParam("minPrice", BigDecimal.valueOf(50.0))
                .queryParam("maxPrice", BigDecimal.valueOf(150.0))
                .queryParam("onlyActive", true)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<ProductDto>>() {})))
                .thenReturn(new ResponseEntity<>(productPageDto, HttpStatus.OK));

        PageDto<ProductDto> result = catalogClient.getProducts(token, 0, 10, 1L, "Test", BigDecimal.valueOf(50.0), BigDecimal.valueOf(150.0), true);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<ProductDto>>() {}));
        verifyAuthorizationHeader();
    }

    @Test
    void getProducts_shouldHandleNullParameters() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<ProductDto>>() {})))
                .thenReturn(new ResponseEntity<>(productPageDto, HttpStatus.OK));

        PageDto<ProductDto> result = catalogClient.getProducts(token, 0, 10, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<ProductDto>>() {}));
        verifyAuthorizationHeader();
    }

    @Test
    void getProducts_shouldThrowHttpClientErrorException() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<ProductDto>>() {})))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.getProducts(token, 0, 10, null, null, null, null, null));
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.GET), any(HttpEntity.class), eq(new ParameterizedTypeReference<PageDto<ProductDto>>() {}));
        verifyAuthorizationHeader();
    }

    // createProduct
    @Test
    void createProduct_shouldCreateProduct() {
        when(restTemplate.postForObject(eq(catalogUrl + "/products"), any(HttpEntity.class), eq(ProductDto.class)))
                .thenReturn(productDto);

        ProductDto result = catalogClient.createProduct(token, productDto);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(restTemplate).postForObject(eq(catalogUrl + "/products"), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void createProduct_shouldThrowHttpClientErrorException() {
        when(restTemplate.postForObject(eq(catalogUrl + "/products"), any(HttpEntity.class), eq(ProductDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.createProduct(token, productDto));
        verify(restTemplate).postForObject(eq(catalogUrl + "/products"), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    // getProduct
    @Test
    void getProduct_shouldReturnProduct() {
        when(restTemplate.exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductDto.class)))
                .thenReturn(new ResponseEntity<>(productDto, HttpStatus.OK));

        ResponseEntity<ProductDto> result = catalogClient.getProduct(token, 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Test Product", result.getBody().getName());
        verify(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void getProduct_shouldThrowHttpClientErrorException() {
        when(restTemplate.exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.getProduct(token, 1L));
        verify(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    // updateProduct
    @Test
    void updateProduct_shouldUpdateProduct() {
        when(restTemplate.exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(ProductDto.class)))
                .thenReturn(new ResponseEntity<>(productDto, HttpStatus.OK));

        ProductDto result = catalogClient.updateProduct(token, 1L, productDto);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void updateProduct_shouldThrowHttpClientErrorException() {
        when(restTemplate.exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(ProductDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.updateProduct(token, 1L, productDto));
        verify(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.PUT), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    // deleteProduct
    @Test
    void deleteProduct_shouldDeleteProduct() {
        doNothing().when(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));

        catalogClient.deleteProduct(token, 1L);

        verify(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verifyAuthorizationHeader();
    }

    @Test
    void deleteProduct_shouldThrowHttpClientErrorException() {
        doThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND))
                .when(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.deleteProduct(token, 1L));
        verify(restTemplate).exchange(eq(catalogUrl + "/products/1"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        verifyAuthorizationHeader();
    }

    // updateStock
    @Test
    void updateStock_shouldUpdateStock() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products/1/stock")
                .queryParam("delta", 5)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(ProductDto.class)))
                .thenReturn(new ResponseEntity<>(productDto, HttpStatus.OK));

        ProductDto result = catalogClient.updateStock(token, 1L, 5);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    @Test
    void updateStock_shouldThrowHttpClientErrorException() {
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products/1/stock")
                .queryParam("delta", 5)
                .toUriString();
        when(restTemplate.exchange(eq(url), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(ProductDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> catalogClient.updateStock(token, 1L, 5));
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(ProductDto.class));
        verifyAuthorizationHeader();
    }

    private void verifyAuthorizationHeader() {
        verify(restTemplate).exchange(anyString(), any(HttpMethod.class), argThat((HttpEntity<?> entity) ->
                entity.getHeaders().get("Authorization").contains("Bearer " + token)), (Class<Object>) any());
    }
}
