// src/main/java/com/restaurante/fastfood/config/DataInitializer.java
package com.restaurante.fastfood.config;

import com.restaurante.fastfood.model.Product;
import com.restaurante.fastfood.model.User;
import com.restaurante.fastfood.model.enums.Role;
import com.restaurante.fastfood.repository.ProductRepository;
import com.restaurante.fastfood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Crear usuarios si no existen
        if (userRepository.count() == 0) {
            createUsers();
        }

        // Crear productos si no existen
        if (productRepository.count() == 0) {
            createProducts();
        }
    }

    private void createUsers() {
        // Usuario administrador
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Administrador");
        admin.setEmail("admin@fastfood.com");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setEnabled(true);

        // Usuario cliente
        User client = new User();
        client.setUsername("cliente");
        client.setPassword(passwordEncoder.encode("cliente123"));
        client.setFullName("Cliente Demo");
        client.setEmail("cliente@example.com");
        client.setAddress("Calle Principal 123");
        client.setPhone("555-123-4567");
        client.setRole(Role.ROLE_CLIENT);
        client.setEnabled(true);

        userRepository.saveAll(Arrays.asList(admin, client));
    }

    private void createProducts() {
        List<Product> products = Arrays.asList(
                new Product(null, "Hamburguesa Clásica", "Deliciosa hamburguesa con carne de res, lechuga, tomate y queso", new BigDecimal("8.99"), "https://via.placeholder.com/300x200?text=Hamburguesa+Clásica", true, "Hamburguesas"),
                new Product(null, "Hamburguesa Doble", "Hamburguesa con doble carne, bacon, queso cheddar y salsa especial", new BigDecimal("12.99"), "https://via.placeholder.com/300x200?text=Hamburguesa+Doble", true, "Hamburguesas"),
                new Product(null, "Hamburguesa Vegetariana", "Hamburguesa vegetariana con lentejas, champiñones y aguacate", new BigDecimal("9.99"), "https://via.placeholder.com/300x200?text=Hamburguesa+Vegetariana", true, "Hamburguesas"),
                new Product(null, "Pizza Margarita", "Pizza tradicional con salsa de tomate, mozzarella y albahaca", new BigDecimal("10.99"), "https://via.placeholder.com/300x200?text=Pizza+Margarita", true, "Pizzas"),
                new Product(null, "Pizza Pepperoni", "Pizza con abundante pepperoni y queso mozzarella", new BigDecimal("12.99"), "https://via.placeholder.com/300x200?text=Pizza+Pepperoni", true, "Pizzas"),
                new Product(null, "Pizza Hawaiana", "Pizza con jamón, piña y queso mozzarella", new BigDecimal("11.99"), "https://via.placeholder.com/300x200?text=Pizza+Hawaiana", true, "Pizzas"),
                new Product(null, "Papas Fritas", "Crujientes papas fritas con sal", new BigDecimal("3.99"), "https://via.placeholder.com/300x200?text=Papas+Fritas", true, "Complementos"),
                new Product(null, "Aros de Cebolla", "Aros de cebolla rebozados y fritos", new BigDecimal("4.99"), "https://via.placeholder.com/300x200?text=Aros+de+Cebolla", true, "Complementos"),
                new Product(null, "Ensalada César", "Ensalada fresca con lechuga, pollo, crutones y aderezo César", new BigDecimal("7.99"), "https://via.placeholder.com/300x200?text=Ensalada+César", true, "Complementos"),
                new Product(null, "Refresco Cola", "Refresco de cola en lata", new BigDecimal("1.99"), "https://via.placeholder.com/300x200?text=Refresco+Cola", true, "Bebidas"),
                new Product(null, "Agua Mineral", "Botella de agua mineral", new BigDecimal("1.50"), "https://via.placeholder.com/300x200?text=Agua+Mineral", true, "Bebidas"),
                new Product(null, "Batido de Chocolate", "Batido cremoso de chocolate", new BigDecimal("4.50"), "https://via.placeholder.com/300x200?text=Batido+de+Chocolate", true, "Bebidas"),
                new Product(null, "Cerveza", "Cerveza artesanal", new BigDecimal("3.99"), "https://via.placeholder.com/300x200?text=Cerveza", true, "Bebidas"),
                new Product(null, "Alitas de Pollo", "Alitas de pollo picantes con salsa BBQ", new BigDecimal("8.99"), "https://via.placeholder.com/300x200?text=Alitas+de+Pollo", true, "Complementos"),
                new Product(null, "Nachos con Queso", "Nachos crujientes con queso fundido y guacamole", new BigDecimal("6.99"), "https://via.placeholder.com/300x200?text=Nachos+con+Queso", true, "Complementos")
        );

        productRepository.saveAll(products);
    }
}
