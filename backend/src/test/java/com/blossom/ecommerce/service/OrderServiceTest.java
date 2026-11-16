
package com.blossom.ecommerce.service;

import com.blossom.ecommerce.dto.*;
import com.blossom.ecommerce.entity.*;
import com.blossom.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private Jwt jwt;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        jwt = mock(Jwt.class);

        user = new User();
        user.setId(1L);
        user.setEmail("cliente@blossom.test");
        user.setRole(Role.CUSTOMER);

        product = new Product();
        product.setId(1L);
        product.setName("Teclado Mecánico RGB");
        product.setPrice(new BigDecimal("189900.00"));
        product.setStock(15);
        product.setActive(true);
    }

    private void mockAuthenticatedUser() {
        when(jwt.getSubject()).thenReturn("1");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
    }

    private CreateOrderRequest request(int quantity) {
        return new CreateOrderRequest(
                List.of(new OrderItemRequest(1L, quantity))
        );
    }

    @Test
    void shouldCreateOrderAndDecreaseStock() {
        mockAuthenticatedUser();

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        OrderResponse result =
                orderService.create(request(2), jwt);

        assertEquals(1L, result.id());
        assertEquals("CREATED", result.status());
        assertEquals(
                new BigDecimal("379800.00"),
                result.total()
        );

        assertEquals(13, product.getStock());
        assertEquals(1, result.items().size());
        assertEquals(2, result.items().get(0).quantity());

        verify(orderRepository).saveAndFlush(any(Order.class));
    }

    @Test
    void shouldRejectInsufficientStock() {
        mockAuthenticatedUser();

        product.setStock(1);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.create(request(2), jwt)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        assertEquals(1, product.getStock());

        verify(orderRepository, never())
                .saveAndFlush(any(Order.class));
    }

    @Test
    void shouldRejectNonExistingProduct() {
        mockAuthenticatedUser();

        when(productRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        CreateOrderRequest request =
                new CreateOrderRequest(
                        List.of(new OrderItemRequest(99L, 1))
                );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.create(request, jwt)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void shouldRejectInactiveProduct() {
        mockAuthenticatedUser();

        product.setActive(false);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.create(request(1), jwt)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    @Test
    void shouldRejectInvalidProductPrice() {
        mockAuthenticatedUser();

        product.setPrice(BigDecimal.ZERO);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.create(request(1), jwt)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }

    @Test
    void shouldCombineRepeatedProducts() {
        mockAuthenticatedUser();

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        when(orderRepository.saveAndFlush(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(2L);
                    return order;
                });

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new OrderItemRequest(1L, 2),
                        new OrderItemRequest(1L, 3)
                )
        );

        OrderResponse result = orderService.create(request, jwt);

        assertEquals(1, result.items().size());
        assertEquals(5, result.items().get(0).quantity());
        assertEquals(10, product.getStock());
        assertEquals(
                new BigDecimal("949500.00"),
                result.total()
        );

        verify(productRepository, times(1))
                .findByIdForUpdate(1L);
    }

    @Test
    void shouldRejectZeroQuantity() {
        mockAuthenticatedUser();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.create(request(0), jwt)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verifyNoInteractions(productRepository, orderRepository);
    }

    @Test
    void shouldRejectQuantityOverflow() {
        mockAuthenticatedUser();

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(
                        new OrderItemRequest(1L, Integer.MAX_VALUE),
                        new OrderItemRequest(1L, 1)
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.create(request, jwt)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    @Test
    void shouldReturnEmptyOrderHistory() {
        when(jwt.getSubject()).thenReturn("1");

        when(orderRepository.findDistinctByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<OrderResponse> result =
                orderService.findMyOrders(jwt);

        assertTrue(result.isEmpty());
        verify(orderRepository)
                .findDistinctByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void shouldRejectInvalidJwtSubject() {
        when(jwt.getSubject()).thenReturn("invalid");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> orderService.findMyOrders(jwt)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(orderRepository);
    }
}
