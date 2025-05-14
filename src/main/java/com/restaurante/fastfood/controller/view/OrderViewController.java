// src/main/java/com/restaurante/fastfood/controller/view/OrderViewController.java
package com.restaurante.fastfood.controller.view;

import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.OrderService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/orders")
@PreAuthorize("hasRole('CLIENT')")
public class OrderViewController {

    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public OrderViewController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String myOrders(Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Order> orders = orderService.findOrdersByUser(user);
        model.addAttribute("orders", orders);
        return "order-history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Authentication authentication, Model model) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Verificar que el usuario sea el propietario del pedido
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "order-detail";
    }
}

