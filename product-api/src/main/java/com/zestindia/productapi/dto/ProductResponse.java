package com.zestindia.productapi.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ProductResponse {
    private Integer id;
    private String productName;
    private String createdBy;
    private Timestamp createdOn;
    private String modifiedBy;
    private Timestamp modifiedOn;
    private List<ItemResponse> items;

    @Data
    public static class ItemResponse {
        private Integer id;
        private Integer quantity;
    }
}