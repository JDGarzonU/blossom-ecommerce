
package com.blossom.ecommerce;

import com.blossom.ecommerce.dto.*;
import com.blossom.ecommerce.entity.*;
import com.blossom.ecommerce.repository.*;
import com.blossom.ecommerce.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

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
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderService orderService;

	private Product createProduct() {
		Product product = new Product();
		product.setName("Teclado Mecánico RGB");
		product.setDescription("Teclado de prueba");
		product.setPrice(new BigDecimal("189900.00"));
		product.setCategory("Perifericos");
		product.setStock(15);
		product.setActive(true);

		return productRepository.saveAndFlush(product);
	}

	@Test
	void contextLoads() {
		assertNotNull(productRepository);
		assertNotNull(orderService);
	}

	@Test
	@Transactional
	void shouldSearchProductsInRealPostgres() {
		Product saved = createProduct();

		var results = productService.search(
				"teclado",
				"perifericos",
				new BigDecimal("100000.00"),
				new BigDecimal("200000.00")
		);

		assertTrue(results.stream()
				.anyMatch(p -> p.id().equals(saved.getId())));
	}

	@Test
	@Transactional
	void shouldCreateOrderAndUpdateRealInventory() {
		User user = new User();
		user.setName("Cliente Integracion");
		user.setEmail("integration@blossom.test");
		user.setPassword("test-hashed-password");
		user.setRole(Role.CUSTOMER);

		user = userRepository.saveAndFlush(user);

		Product product = createProduct();

		Jwt jwt = Jwt.withTokenValue("integration-test-token")
				.header("alg", "none")
				.subject(user.getId().toString())
				.build();

		CreateOrderRequest request = new CreateOrderRequest(
				java.util.List.of(
						new OrderItemRequest(product.getId(), 2)
				)
		);

		OrderResponse response = orderService.create(request, jwt);

		assertNotNull(response.id());
		assertEquals("CREATED", response.status());
		assertEquals(
				0,
				response.total().compareTo(
						new BigDecimal("379800.00")
				)
		);

		Product updated = productRepository
				.findById(product.getId())
				.orElseThrow();

		assertEquals(13, updated.getStock());

		var history = orderService.findMyOrders(jwt);

		assertEquals(1, history.size());
		assertEquals(response.id(), history.get(0).id());
	}
}
