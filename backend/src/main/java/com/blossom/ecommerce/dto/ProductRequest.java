
package com.blossom.ecommerce.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotBlank
        @Size(max = 100)
        String category,

        @NotNull
        @Min(0)
        Integer stock,

        @Size(max = 500)
        String imageUrl

) {}
