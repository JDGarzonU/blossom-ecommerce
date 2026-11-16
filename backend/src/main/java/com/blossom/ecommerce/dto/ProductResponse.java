
package com.blossom.ecommerce.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        Integer stock,
        String imageUrl,
        Boolean active
) {}
