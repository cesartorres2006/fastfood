// src/main/java/com/restaurante/fastfood/repository/OrderItemRepository.java
package com.restaurante.fastfood.repository;

import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}

