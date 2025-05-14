package com.restaurante.fastfood.controller.view;

import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.service.ProductService;
import com.restaurante.fastfood.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    @Autowired
    public HomeController(ProductService productService, UserService userService, PasswordEncoder passwordEncoder) {
        this.productService = productService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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
    public String registerUser(@ModelAttribute User user) {
        userService.registerNewUser(user);
        return "redirect:/login";
    }
    // @GetMapping("/checkout")
    // public String checkout(Authentication authentication, Model model) {
    //     if (authentication != null) {
    //         User user = userService.findByUsername(authentication.getName()).orElse(new User());
    //         model.addAttribute("user", user);
    //     }
    //     return "checkout";
    // }

    @GetMapping("/order-confirmation")
    public String orderConfirmation() {
        return "order-confirmation";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }
}


