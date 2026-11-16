
package com.blossom.ecommerce.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}
