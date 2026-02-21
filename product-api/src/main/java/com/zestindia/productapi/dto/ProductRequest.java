package com.zestindia.productapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String productName;

    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }
}