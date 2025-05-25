package com.restaurante.fastfood.controller.view;

import com.restaurante.fastfood.model.Cart;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.CartService;
import com.restaurante.fastfood.service.ProductService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    private final ProductService productService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;

    @Autowired
    public HomeController(ProductService productService, UserService userService, PasswordEncoder passwordEncoder, CartService cartService) {
        this.productService = productService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.findAvailableProducts());
        return "index";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.findAvailableProducts());
        return "products";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, org.springframework.ui.Model model) {
        try {
            userService.registerNewUser(user);
            return "redirect:/login";
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("usuario")) {
                model.addAttribute("usernameError", "Nombre de usuario ya existe");
            }
            if (msg != null && msg.contains("correo")) {
                model.addAttribute("emailError", "Correo electrónico ya en uso");
            }
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/order-confirmation")
    public String orderConfirmation() {
        return "order-confirmation";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @ModelAttribute("cart")
    public Cart cart(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                return cartService.getOrCreateCart(user);
            }
        }
        return new Cart();
    }
}
