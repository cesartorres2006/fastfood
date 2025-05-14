// src/main/java/com/restaurante/fastfood/repository/OrderRepository.java
package com.restaurante.fastfood.repository;

import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStatus(Order.OrderStatus status);
}

