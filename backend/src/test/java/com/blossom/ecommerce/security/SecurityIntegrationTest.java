
package com.blossom.ecommerce.security;

import com.blossom.ecommerce.controller.ProductController;
import com.blossom.ecommerce.dto.ProductResponse;
import com.blossom.ecommerce.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=120"
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private ProductService productService;

    private String token(String role, long expiresInSeconds) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("blossom-ecommerce")
                .subject("2")
                .issuedAt(now.minusSeconds(7200))
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .claim("role", role)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    private String validProductJson() {
        return """
                {
                  "name": "Teclado Mecánico RGB",
                  "description": "Producto de prueba",
                  "price": 189900.00,
                  "category": "Perifericos",
                  "stock": 15
                }
                """;
    }

    private ProductResponse productResponse() {
        return new ProductResponse(
                1L,
                "Teclado Mecánico RGB",
                "Producto de prueba",
                new BigDecimal("189900.00"),
                "Perifericos",
                15,
                null,
                true
        );
    }

    @Test
    void catalogShouldBePublic() throws Exception {
        when(productService.search(
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(List.of(productResponse()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void anonymousUserCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void customerCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER", 3600))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        when(productService.create(any()))
                .thenReturn(productResponse());

        mockMvc.perform(post("/api/products")
                        .header("Authorization",
                                "Bearer " + token("ADMIN", 3600))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(productService).create(any());
    }

    @Test
    void customerCannotDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER", 3600)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(productService);
    }

    @Test
    void adminCanDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization",
                                "Bearer " + token("ADMIN", 3600)))
                .andExpect(status().isNoContent());

        verify(productService).delete(1L);
    }

    @Test
    void invalidTokenShouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization",
                                "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    void expiredTokenShouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/products")
                        .header("Authorization",
                                "Bearer " + token("ADMIN", -3600))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }
}
