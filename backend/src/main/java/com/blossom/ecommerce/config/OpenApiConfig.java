
package com.blossom.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blossomOpenAPI() {
        String scheme = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Blossom E-commerce API")
                        .version("1.0.0")
                        .description(
                                "REST API for products, JWT authentication, " +
                                        "orders and purchase history."))
                .addSecurityItem(
                        new SecurityRequirement().addList(scheme))
                .components(new Components()
                        .addSecuritySchemes(scheme,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
