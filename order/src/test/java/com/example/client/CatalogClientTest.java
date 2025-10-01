package com.example.client;

import com.example.dto.catalog.ProductDto;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@TestPropertySource(properties = {
        "catalog.url=http://localhost:8081"
})
class CatalogClientTest {

    @Autowired
    private CatalogClient catalogClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    private MockRestServiceServer server;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.createServer(catalogClient.getRestTemplate());
        when(jwtUtil.generateServiceToken()).thenReturn("service-token");

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Product");
        productDto.setPrice(BigDecimal.TEN);
        productDto.setStock(100);
        productDto.setActive(true);
    }

    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        server.expect(requestTo("http://localhost:8081/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(productDto), MediaType.APPLICATION_JSON));

        ProductDto result = catalogClient.getProduct(1L, "test-token");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Product");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(result.getStock()).isEqualTo(100);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getProduct_shouldThrowNotFoundException() {
        server.expect(requestTo("http://localhost:8081/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(NotFoundException.class, () -> catalogClient.getProduct(1L, "test-token"));
    }

    @Test
    void getProduct_shouldThrowValidationException() {
        server.expect(requestTo("http://localhost:8081/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThrows(ValidationException.class, () -> catalogClient.getProduct(1L, "test-token"));
    }

    @Test
    void getProduct_shouldThrowRuntimeException() {
        server.expect(requestTo("http://localhost:8081/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(RuntimeException.class, () -> catalogClient.getProduct(1L, "test-token"));
    }

    @Test
    void updateStock_shouldReturnProduct() throws Exception {
        ProductDto updatedProduct = new ProductDto();
        updatedProduct.setId(1L);
        updatedProduct.setName("Product");
        updatedProduct.setPrice(BigDecimal.TEN);
        updatedProduct.setStock(95);
        updatedProduct.setActive(true);

        server.expect(requestTo("http://localhost:8081/products/1/stock?delta=-5"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(updatedProduct), MediaType.APPLICATION_JSON));

        ProductDto result = catalogClient.updateStock(1L, -5, "test-token");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStock()).isEqualTo(95);
    }

    @Test
    void updateStock_shouldThrowNotFoundException() {
        server.expect(requestTo("http://localhost:8081/products/1/stock?delta=-5"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(NotFoundException.class, () -> catalogClient.updateStock(1L, -5, "test-token"));
    }

    @Test
    void updateStock_shouldThrowValidationException() {
        server.expect(requestTo("http://localhost:8081/products/1/stock?delta=-5"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThrows(ValidationException.class, () -> catalogClient.updateStock(1L, -5, "test-token"));
    }

    @Test
    void updateStock_shouldThrowRuntimeException() {
        server.expect(requestTo("http://localhost:8081/products/1/stock?delta=-5"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer service-token"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(RuntimeException.class, () -> catalogClient.updateStock(1L, -5, "test-token"));
    }
}
