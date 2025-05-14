package com.restaurante.fastfood.controller.api;

import com.restaurante.fastfood.model.Cart;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.CartService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CartApiController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return cartService.getCart(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(cartService.getOrCreateCart(user)));
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Cart> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String notes) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            Cart updatedCart = cartService.addProductToCart(user, productId, quantity, notes);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Cart> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam int quantity) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            Cart updatedCart = cartService.updateCartItemQuantity(user, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Cart> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cart updatedCart = cartService.removeProductFromCart(user, productId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Cart> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cart emptyCart = cartService.clearCart(user);
        return ResponseEntity.ok(emptyCart);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Integer> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        // Verificar si el usuario es un cliente
        if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
            Map<String, Integer> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        int count = cartService.getCart(user)
                .map(Cart::getTotalItems)
                .orElse(0);

        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}

