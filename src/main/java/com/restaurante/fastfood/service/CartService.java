package com.restaurante.fastfood.service;

import com.restaurante.fastfood.model.*;
import com.restaurante.fastfood.repository.CartItemRepository;
import com.restaurante.fastfood.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    /**
     * Obtiene el carrito del usuario, o crea uno nuevo si no existe
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCreatedAt(LocalDateTime.now());
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Añade un producto al carrito
     */
    @Transactional
    public Cart addProductToCart(User user, Long productId, int quantity, String notes) {
        // Validar que la cantidad sea positiva
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        // Obtener el carrito
        Cart cart = getOrCreateCart(user);

        // Obtener el producto
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar si el producto ya está en el carrito
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Actualizar la cantidad
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > 20) {
                if (item.getQuantity() == 20) {
                    throw new IllegalArgumentException("Ya tienes 20 unidades de este producto en el carrito. El límite es 20 por producto por pedido.");
                } else {
                    throw new IllegalArgumentException("El límite es 20 unidades por producto por pedido.");
                }
            }
            item.setQuantity(newQuantity);
            item.setNotes(notes);
            cartItemRepository.save(item);
        } else {
            if (quantity > 20) {
                throw new IllegalArgumentException("El límite es 20 unidades por producto por pedido.");
            }
            // Crear nuevo item
            CartItem newItem = new CartItem(product, quantity, notes, product.getPrice());
            newItem.setCart(cart);
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        // Recalcular el total
        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    @Transactional
    public Cart updateCartItemQuantity(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);

        // Si la cantidad es 0 o menos, eliminar el producto
        if (quantity <= 0) {
            // Buscar el producto en el carrito
            Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                cart.getItems().remove(item);
                cartItemRepository.delete(item);
                cart.calculateTotal();
                return cartRepository.save(cart);
            } else {
                return cart;
            }
        }

        // Obtener el producto
        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            if (quantity > 20) {
                throw new IllegalArgumentException("El límite es 20 unidades por producto por pedido.");
            }
            existingItem.get().setQuantity(quantity);
            cartItemRepository.save(existingItem.get());
        } else {
            if (quantity > 20) {
                throw new IllegalArgumentException("El límite es 20 unidades por producto por pedido.");
            }
            CartItem newItem = new CartItem(product, quantity, null, product.getPrice());
            newItem.setCart(cart);
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    /**
     * Elimina un producto del carrito
     */
    @Transactional
    public Cart removeProductFromCart(User user, Long productId) {
        Cart cart = getOrCreateCart(user);
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);

            cart.calculateTotal();
            return cartRepository.save(cart);
        }

        return cart;
    }

    /**
     * Vacía el carrito
     */
    @Transactional
    public Cart clearCart(User user) {
        Cart cart = getOrCreateCart(user);

        // Eliminar todos los items
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    /**
     * Obtiene el carrito del usuario
     */
    public Optional<Cart> getCart(User user) {
        return cartRepository.findByUser(user);
    }

    /**
     * Convierte un carrito en un pedido
     */
    @Transactional
    public Order createOrderFromCart(User user, String deliveryAddress, String contactPhone) {
        Cart cart = getOrCreateCart(user);

        if (cart.isEmpty()) {
            throw new IllegalStateException("No se puede crear un pedido con un carrito vacío");
        }

        // Crear nuevo pedido
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryAddress(deliveryAddress);
        order.setContactPhone(contactPhone);
        order.setCustomerName(user.getFullName());
        order.setStatus(Order.OrderStatus.PENDING);

        // Transferir items del carrito al pedido
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getNotes()
            );
            order.addItem(orderItem);
        }

        // Limpiar el carrito
        clearCart(user);

        return order;
    }
}
