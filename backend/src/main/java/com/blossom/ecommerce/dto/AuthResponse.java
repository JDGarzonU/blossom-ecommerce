
package com.blossom.ecommerce.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String name,
        String email,
        String role
) {}
