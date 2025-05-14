package com.restaurante.fastfood.controller;

import com.restaurante.fastfood.model.Cart;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.CartService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public String viewCart(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cart cart = cartService.getOrCreateCart(user);
        model.addAttribute("cart", cart);

        return "cart";
    }
}

