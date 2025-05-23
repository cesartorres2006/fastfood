package com.restaurante.fastfood.model;

import jakarta.persistence.*;
        import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private BigDecimal price; // Usa el tipo que corresponde a tu base de datos

    @Column(nullable = false)
    private Integer quantity;

    private String notes;

    // Constructor para facilitar la creación de items
    public CartItem(Product product, Integer quantity, String notes, BigDecimal price) {
        this.product = product;
        this.quantity = quantity;
        this.notes = notes;
        this.price = price;
    }
}

