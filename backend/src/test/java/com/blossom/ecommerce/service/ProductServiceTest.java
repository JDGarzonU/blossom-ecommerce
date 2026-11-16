
package com.blossom.ecommerce.service;

import com.blossom.ecommerce.dto.ProductRequest;
import com.blossom.ecommerce.dto.ProductResponse;
import com.blossom.ecommerce.entity.Product;
import com.blossom.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Teclado Mecánico RGB");
        product.setDescription("Teclado para gaming");
        product.setPrice(new BigDecimal("189900.00"));
        product.setCategory("Perifericos");
        product.setStock(15);
        product.setActive(true);

        request = new ProductRequest(
                "Teclado Mecánico RGB",
                "Teclado para gaming",
                new BigDecimal("189900.00"),
                "Perifericos",
                15,
                null
        );
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        ProductResponse result = productService.create(request);

        assertEquals(1L, result.id());
        assertEquals("Teclado Mecánico RGB", result.name());
        assertEquals(15, result.stock());
        assertTrue(result.active());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldFindProductById() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse result = productService.findById(1L);

        assertEquals(1L, result.id());
        assertEquals(new BigDecimal("189900.00"), result.price());
    }

    @Test
    void shouldReturn404WhenProductDoesNotExist() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.findById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND,
                exception.getStatusCode());
    }

    @Test
    void shouldNotReturnInactiveProduct() {
        product.setActive(false);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.findById(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND,
                exception.getStatusCode());
    }

    @Test
    void shouldUpdateProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductRequest updated = new ProductRequest(
                "Teclado Pro",
                "Versión actualizada",
                new BigDecimal("199900.00"),
                "Perifericos",
                20,
                null
        );

        ProductResponse result =
                productService.update(1L, updated);

        assertEquals("Teclado Pro", result.name());
        assertEquals(20, result.stock());
        assertEquals(new BigDecimal("199900.00"),
                result.price());
    }

    @Test
    void shouldSoftDeleteProduct() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.delete(1L);

        assertFalse(product.getActive());
        verify(productRepository).save(product);
    }

    @Test
    void shouldSearchProducts() {
        when(productRepository.findAll(
                any(Specification.class),
                any(Sort.class)
        )).thenReturn(List.of(product));

        List<ProductResponse> results =
                productService.search(
                        "Teclado",
                        "Perifericos",
                        new BigDecimal("100000"),
                        new BigDecimal("200000")
                );

        assertEquals(1, results.size());
        assertEquals("Teclado Mecánico RGB",
                results.get(0).name());
    }

    @Test
    void shouldRejectInvalidPriceRange() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productService.search(
                        null,
                        null,
                        new BigDecimal("200000"),
                        new BigDecimal("100000")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                exception.getStatusCode());

        verifyNoInteractions(productRepository);
    }

    @Test
    void shouldRejectNegativePriceFilter() {
        assertThrows(
                ResponseStatusException.class,
                () -> productService.search(
                        null,
                        null,
                        new BigDecimal("-1"),
                        null
                )
        );

        verifyNoInteractions(productRepository);
    }
}
