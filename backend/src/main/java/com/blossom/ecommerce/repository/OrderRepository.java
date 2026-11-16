
package com.blossom.ecommerce.repository;

import com.blossom.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    List<Order> findDistinctByUserIdOrderByCreatedAtDesc(Long userId);
}
