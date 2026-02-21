package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.ProductRequest;
import com.zestindia.productapi.dto.ProductResponse;
import com.zestindia.productapi.entity.Item;
import com.zestindia.productapi.entity.Product;
import com.zestindia.productapi.repository.ItemRepository;
import com.zestindia.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public ProductResponse getProductById(Integer id) {
        return productRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public ProductResponse createProduct(ProductRequest request, Authentication auth) {
        Product product = toEntity(request, auth.getName());

        // Ensure items list is never null (extra safety)
        if (product.getItems() == null) {
            product.setItems(new ArrayList<>());
        }

        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Integer id, ProductRequest request, Authentication auth) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setProductName(request.getProductName());
        product.setModifiedBy(auth.getName());

        // Clear existing items safely
        if (product.getItems() != null) {
            product.getItems().clear();
        } else {
            product.setItems(new ArrayList<>());
        }

        // Add new items
        if (request.getItems() != null) {
            request.getItems().forEach(itemReq -> {
                Item item = new Item();
                item.setQuantity(itemReq.getQuantity());
                item.setProduct(product);
                product.getItems().add(item);
            });
        }

        return toResponse(productRepository.save(product));
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    public List<ProductResponse.ItemResponse> getProductItems(Integer id) {
        return itemRepository.findByProductId(id).stream()
                .map(item -> {
                    ProductResponse.ItemResponse resp = new ProductResponse.ItemResponse();
                    resp.setId(item.getId());
                    resp.setQuantity(item.getQuantity());
                    return resp;
                }).collect(Collectors.toList());
    }

    private Product toEntity(ProductRequest req, String createdBy) {
        Product product = new Product();
        product.setProductName(req.getProductName());
        product.setCreatedBy(createdBy);
        req.getItems().forEach(itemReq -> {
            Item item = new Item();
            item.setQuantity(itemReq.getQuantity());
            item.setProduct(product);
            product.getItems().add(item);
        });
        return product;
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse resp = new ProductResponse();
        resp.setId(product.getId());
        resp.setProductName(product.getProductName());
        resp.setCreatedBy(product.getCreatedBy());
        resp.setCreatedOn(product.getCreatedOn());
        resp.setModifiedBy(product.getModifiedBy());
        resp.setModifiedOn(product.getModifiedOn());
        resp.setItems(product.getItems().stream().map(item -> {
            ProductResponse.ItemResponse itemResp = new ProductResponse.ItemResponse();
            itemResp.setId(item.getId());
            itemResp.setQuantity(item.getQuantity());
            return itemResp;
        }).collect(Collectors.toList()));
        return resp;
    }

    @Async
    public void logAudit(String action) {
        // Simulate async logging
        System.out.println("Async audit log: " + action);
    }
}