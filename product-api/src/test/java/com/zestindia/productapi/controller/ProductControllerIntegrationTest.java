package com.zestindia.productapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestindia.productapi.dto.AuthRequest;
import com.zestindia.productapi.dto.AuthResponse;
import com.zestindia.productapi.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // uses application-test.properties with H2
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login as admin to get token
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("pass123");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);

        adminToken = "Bearer " + authResponse.getAccessToken();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_shouldReturn201() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductName("Integration Test Phone");
        ProductRequest.ItemRequest item = new ProductRequest.ItemRequest();
        item.setQuantity(100);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Integration Test Phone"))
                .andExpect(jsonPath("$.createdBy").value("admin"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].quantity").value(100));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createProduct_withUserRole_shouldReturn403() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setProductName("Forbidden Product");

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", adminToken) // even with token, role USER → 403
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllProducts_noAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    // Add more tests:
    // - GET with pagination
    // - GET single product
    // - PUT / DELETE with correct & wrong roles
    // - Validation errors (400)
}