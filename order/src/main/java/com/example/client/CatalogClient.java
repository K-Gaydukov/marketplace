package com.example.client;

import com.example.dto.catalog.ProductDto;
import com.example.exception.NotFoundException;
import com.example.exception.ValidationException;
import com.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Component
public class CatalogClient {
    private final RestTemplate restTemplate;
    private final String catalogUrl;
    private final JwtUtil jwtUtil;

    public CatalogClient(RestTemplate restTemplate,
                         @Value("${catalog.url}") String catalogUrl,
                         JwtUtil jwtUtil) {
        this.restTemplate = restTemplate;
        this.catalogUrl = catalogUrl;
        this.jwtUtil = jwtUtil;
    }

    public ProductDto getProduct(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(
                    catalogUrl + "/products/" + id, HttpMethod.GET, entity, ProductDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Product not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Product inactive or unavailable");
            } else {
                throw new RuntimeException("Catalog error: " + e.getMessage());
            }
        }
    }

    public ProductDto updateStock(Long id, Integer delta, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtUtil.generateServiceToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = UriComponentsBuilder.fromHttpUrl(catalogUrl + "/products/" + id + "/stock")
                .queryParam("delta", delta)
                .toUriString();
        try {
            return restTemplate.exchange(url, HttpMethod.PATCH, entity, ProductDto.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new NotFoundException("Product not found");
            } else if (e.getStatusCode().value() == 422) {
                throw new ValidationException("Invalid stock update");
            } else {
                throw new RuntimeException("Stock update error: " + e.getMessage());
            }
        }
    }

    // for tests order
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
