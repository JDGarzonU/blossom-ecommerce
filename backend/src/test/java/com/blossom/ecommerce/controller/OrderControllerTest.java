
package com.blossom.ecommerce.controller;

import com.blossom.ecommerce.dto.OrderItemResponse;
import com.blossom.ecommerce.dto.OrderResponse;
import com.blossom.ecommerce.security.SecurityConfig;
import com.blossom.ecommerce.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=120"
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private OrderService orderService;

    private String token(String role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuer("blossom-ecommerce")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("role", role)
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    private OrderResponse orderResponse() {
        return new OrderResponse(
                1L,
                "CREATED",
                new BigDecimal("379800.00"),
                LocalDateTime.now(),
                List.of(
                        new OrderItemResponse(
                                1L,
                                "Teclado Mecánico RGB",
                                2,
                                new BigDecimal("189900.00"),
                                new BigDecimal("379800.00")
                        )
                )
        );
    }

    @Test
    void shouldCreateOrderWithCustomerToken() throws Exception {
        when(orderService.create(any(), any(Jwt.class)))
                .thenReturn(orderResponse());

        String json = """
                {
                  "items": [
                    {
                      "productId": 1,
                      "quantity": 2
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total").value(379800.00))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(orderService).create(any(), any(Jwt.class));
    }

    @Test
    void shouldGetAuthenticatedUserOrders() throws Exception {
        when(orderService.findMyOrders(any(Jwt.class)))
                .thenReturn(List.of(orderResponse()));

        mockMvc.perform(get("/api/orders/my-orders")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].items[0].productId")
                        .value(1));

        verify(orderService).findMyOrders(any(Jwt.class));
    }

    @Test
    void shouldRejectOrderWithoutToken() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":1,"quantity":2}]}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectOrderHistoryWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectEmptyOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectZeroQuantity() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId":1,"quantity":0}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturnConflictForInsufficientStock()
            throws Exception {

        when(orderService.create(any(), any(Jwt.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock: 1"
                ));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization",
                                "Bearer " + token("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"productId":1,"quantity":999}
                                  ]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
