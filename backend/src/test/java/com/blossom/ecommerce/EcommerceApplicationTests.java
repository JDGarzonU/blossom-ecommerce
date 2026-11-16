
package com.blossom.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class EcommerceApplicationTests {

	@Container
	static PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:17")
					.withDatabaseName("blossom_test")
					.withUsername("test_user")
					.withPassword("test_password");

	@DynamicPropertySource
	static void configureDatabase(DynamicPropertyRegistry registry) {
		registry.add(
				"spring.datasource.url",
				postgres::getJdbcUrl
		);
		registry.add(
				"spring.datasource.username",
				postgres::getUsername
		);
		registry.add(
				"spring.datasource.password",
				postgres::getPassword
		);
	}

	@Test
	void contextLoads() {
		// Verifica que Spring inicie con PostgreSQL temporal.
	}
}
