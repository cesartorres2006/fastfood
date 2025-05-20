// src/main/java/com/restaurante/fastfood/controller/CheckoutController.java
package com.restaurante.fastfood.controller;

import com.restaurante.fastfood.model.Cart;
import com.restaurante.fastfood.model.Order;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.CartService;
import com.restaurante.fastfood.service.OrderService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    @Autowired
    public CheckoutController(CartService cartService, OrderService orderService, UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public String checkoutPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Cart cart = cartService.getOrCreateCart(user);

        // Verificar si el carrito está vacío
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        // Verificar si hay demasiados productos
        if (cart.hasTooManyItems()) {
            model.addAttribute("error", "Un pedido no puede tener más de 10 productos");
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);

        return "checkout/index";
    }

    @PostMapping("/place-order")
    @PreAuthorize("hasRole('CLIENT')")
    public String placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String deliveryAddress,
            @RequestParam String contactPhone,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            // Crear pedido a partir del carrito
            Order order = cartService.createOrderFromCart(user, deliveryAddress, contactPhone);

            // Guardar el pedido
            Order savedOrder = orderService.createOrder(order);

            redirectAttributes.addFlashAttribute("success", "¡Pedido realizado con éxito! Número de pedido: " + savedOrder.getId());
            return "redirect:/orders";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "No se puede crear un pedido con un carrito vacío");
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ha ocurrido un error al procesar su pedido");
            return "redirect:/checkout";
        }
    }
}
