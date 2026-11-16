
package com.blossom.ecommerce.service;

import com.blossom.ecommerce.dto.ProductRequest;
import com.blossom.ecommerce.dto.ProductResponse;
import com.blossom.ecommerce.entity.Product;
import com.blossom.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.Locale;


import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyChanges(product, request);
        product.setActive(true);

        return toResponse(productRepository.save(product));
    }


    @Transactional(readOnly = true)
    public List<ProductResponse> search(
            String name,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        if ((minPrice != null && minPrice.signum() < 0)
                || (maxPrice != null && maxPrice.signum() < 0)
                || (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid price range"
            );
        }

        Specification<Product> specification =
                (root, query, cb) ->
                        cb.isTrue(root.get("active"));

        if (name != null && !name.isBlank()) {
            String searchName = "%" +
                    name.trim().toLowerCase(Locale.ROOT) + "%";

            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("name")),
                            searchName
                    )
            );
        }

        if (category != null && !category.isBlank()) {
            String searchCategory =
                    category.trim().toLowerCase(Locale.ROOT);

            specification = specification.and(
                    (root, query, cb) -> cb.equal(
                            cb.lower(root.get("category")),
                            searchCategory
                    )
            );
        }

        if (minPrice != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("price"),
                                    minPrice
                            )
            );
        }

        if (maxPrice != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("price"),
                                    maxPrice
                            )
            );
        }

        return productRepository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "id")
        ).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(findActiveProduct(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findActiveProduct(id);
        applyChanges(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findActiveProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findActiveProduct(Long id) {
        return productRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));
    }

    private void applyChanges(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category().trim());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStock(),
                product.getImageUrl(),
                product.getActive()
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null : value.trim();
    }
}
