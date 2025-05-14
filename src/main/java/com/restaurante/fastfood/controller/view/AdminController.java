// src/main/java/com/restaurante/fastfood/controller/view/AdminController.java
package com.restaurante.fastfood.controller.view;

import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.Product;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.OrderService;
import com.restaurante.fastfood.service.ProductService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public AdminController(OrderService orderService, ProductService productService, UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalOrders", orderService.findAllOrders().size());
        model.addAttribute("pendingOrders", orderService.findOrdersByStatus(Order.OrderStatus.PENDING).size());
        model.addAttribute("totalProducts", productService.findAllProducts().size());
        model.addAttribute("totalUsers", userService.findAllUsers().size());
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "admin/orders";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAllProducts());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product) {
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        productService.findById(id).ifPresent(product -> model.addAttribute("product", product));
        return "admin/product-form";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        orderService.findById(id).ifPresent(order -> model.addAttribute("order", order));
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/admin/orders";
    }
}

