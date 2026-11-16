
package com.blossom.ecommerce.controller;

import com.blossom.ecommerce.dto.ProductResponse;
import com.blossom.ecommerce.exception.GlobalExceptionHandler;
import com.blossom.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    private ProductResponse product() {
        return new ProductResponse(
                1L,
                "Teclado Mecánico RGB",
                "Teclado para gaming",
                new BigDecimal("189900.00"),
                "Perifericos",
                15,
                null,
                true
        );
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductController(productService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListProducts() throws Exception {
        when(productService.search(
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(List.of(product()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Teclado Mecánico RGB"));
    }

    @Test
    void shouldFilterProductsByCategory() throws Exception {
        when(productService.search(
                isNull(), eq("Perifericos"), isNull(), isNull()
        )).thenReturn(List.of(product()));

        mockMvc.perform(get("/api/products")
                        .param("category", "Perifericos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category")
                        .value("Perifericos"));
    }

    @Test
    void shouldFindProductById() throws Exception {
        when(productService.findById(1L))
                .thenReturn(product());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404ForUnknownProduct() throws Exception {
        when(productService.findById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Product not found"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        when(productService.create(any()))
                .thenReturn(product());

        String json = """
                {
                  "name": "Teclado Mecánico RGB",
                  "description": "Teclado para gaming",
                  "price": 189900.00,
                  "category": "Perifericos",
                  "stock": 15
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(productService).create(any());
    }
}
