package com.restaurante.fastfood.repository;

import com.restaurante.fastfood.model.Cart;
import com.restaurante.fastfood.model.CartItem;
import com.restaurante.fastfood.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
