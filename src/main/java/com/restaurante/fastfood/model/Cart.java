package com.restaurante.fastfood.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Método para calcular el total del carrito
    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método para añadir un item al carrito
    public void addItem(CartItem item) {
        // Buscar si ya existe un item con el mismo producto
        for (CartItem existingItem : items) {
            if (existingItem.getProduct().getId().equals(item.getProduct().getId())) {
                // Si existe, incrementar la cantidad
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                calculateTotal();
                return;
            }
        }

        // Si no existe, añadir el nuevo item
        items.add(item);
        item.setCart(this);
        calculateTotal();
    }

    // Método para actualizar la cantidad de un item
    public void updateItemQuantity(Long productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(productId)) {
                if (quantity <= 0) {
                    // Si la cantidad es 0 o menos, eliminar el item
                    removeItem(item);
                } else {
                    // Si no, actualizar la cantidad
                    item.setQuantity(quantity);
                }
                calculateTotal();
                return;
            }
        }
    }

    // Método para eliminar un item del carrito
    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
        calculateTotal();
    }

    // Método para eliminar un item por ID de producto
    public void removeItemByProductId(Long productId) {
        items.removeIf(item -> {
            if (item.getProduct().getId().equals(productId)) {
                item.setCart(null);
                return true;
            }
            return false;
        });
        calculateTotal();
    }

    // Método para limpiar el carrito
    public void clear() {
        items.clear();
        totalAmount = BigDecimal.ZERO;
    }

    // Método para verificar si el carrito está vacío
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Método para obtener el número total de items en el carrito
    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Método para verificar si el carrito tiene demasiados items (más de 10)
    public boolean hasTooManyItems() {
        return getTotalItems() > 10;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

