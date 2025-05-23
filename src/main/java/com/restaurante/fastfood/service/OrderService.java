// src/main/java/com/restaurante/fastfood/service/OrderService.java
package com.restaurante.fastfood.service;

import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.OrderItem;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> findOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order createOrder(Order order) {
        // Establecer fecha de pedido
        order.setOrderDate(LocalDateTime.now());

        // Establecer estado inicial
        order.setStatus(Order.OrderStatus.PENDING);

        // Calcular el total
        order.calculateTotal();

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public Order addItemToOrder(Order order, OrderItem item) {
        order.addItem(item);
        return orderRepository.save(order);
    }

    public List<Order> findOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }

    public List<Order> findOrdersFiltered(String status, LocalDateTime from, LocalDateTime to) {
        if (status != null && !status.isEmpty() && from != null && to != null) {
            return orderRepository.findByStatusAndOrderDateBetween(Order.OrderStatus.valueOf(status), from, to);
        } else if (status != null && !status.isEmpty()) {
            return orderRepository.findByStatus(Order.OrderStatus.valueOf(status));
        } else if (from != null && to != null) {
            return orderRepository.findByOrderDateBetween(from, to);
        } else {
            return orderRepository.findAll();
        }
    }
}
