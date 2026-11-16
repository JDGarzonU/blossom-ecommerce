
package com.blossom.ecommerce.controller;

import com.blossom.ecommerce.dto.CreateOrderRequest;
import com.blossom.ecommerce.dto.OrderResponse;
import com.blossom.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.create(request, jwt));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> myOrders(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                orderService.findMyOrders(jwt)
        );
    }
}
