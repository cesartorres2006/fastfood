// src/main/java/com/restaurante/fastfood/model/OrderItem.java
package com.restaurante.fastfood.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    private String notes;

    // Constructor para facilitar la creación de items
    public OrderItem(Product product, Integer quantity, String notes) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
        this.notes = notes;
    }
}

