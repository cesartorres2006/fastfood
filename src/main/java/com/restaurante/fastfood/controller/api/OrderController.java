// src/main/java/com/restaurante/fastfood/controller/api/OrderController.java
package com.restaurante.fastfood.controller.api;

import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.OrderService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Order> createOrder(@RequestBody Order order, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        order.setUser(user);
        Order savedOrder = orderService.createOrder(order);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(orderService.findOrdersByUser(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Verificar que el usuario sea el propietario del pedido o un administrador
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ||
                order.getUser().getUsername().equals(userDetails.getUsername())) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id, @RequestBody Order.OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Estado actualizado correctamente");
        return ResponseEntity.ok(response);
    }

    // Elimina o comenta este endpoint duplicado para evitar conflicto con AdminOrderApiController
    // @GetMapping("/all")
    // public ResponseEntity<List<Order>> getAllOrders() {
    //     return ResponseEntity.ok(orderService.findAllOrders());
    // }
}
