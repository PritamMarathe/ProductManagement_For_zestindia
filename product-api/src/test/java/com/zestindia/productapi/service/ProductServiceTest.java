package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.repository.ProductRepository;
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
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Authentication mockAuth;

    @BeforeEach
    void setUp() {
        mockAuth = mock(Authentication.class);
        when(mockAuth.getName()).thenReturn("admin");
    }

    @Test
    void getAllProducts_shouldReturnPagedResult() {
        // given
        Product product = new Product();
        product.setId(1);
        product.setProductName("Test Product");

        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        Page<ProductResponse> result = productService.getAllProducts(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductName()).isEqualTo("Test Product");
        verify(productRepository).findAll(any(Pageable.class));
    }

    @Test
    void createProduct_shouldSaveAndReturnResponse() {
        // given
        ProductRequest request = new ProductRequest();
        request.setProductName("New Mobile");
        ProductRequest.ItemRequest itemReq = new ProductRequest.ItemRequest();
        itemReq.setQuantity(5);
        request.setItems(List.of(itemReq));

        Product savedProduct = new Product();
        savedProduct.setId(1);
        savedProduct.setProductName("New Mobile");
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        ProductResponse response = productService.createProduct(request, mockAuth);

        // then
        assertThat(response.getProductName()).isEqualTo("New Mobile");
        assertThat(response.getCreatedBy()).isEqualTo("admin");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_notFound_shouldThrowException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // when + then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> productService.getProductById(999));
    }

    // Add more: update, delete, getItems, validation cases, etc.
}