// src/main/java/com/restaurante/fastfood/service/ProductService.java
package com.restaurante.fastfood.service;

import com.restaurante.fastfood.model.Product;
import com.restaurante.fastfood.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> findAvailableProducts() {
        return productRepository.findByAvailableTrue();
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    // Nuevo método para búsqueda combinada, ahora insensible a tildes
    public List<Product> searchProducts(String query, String category) {
        // Normalizar la query: quitar tildes y pasar a minúsculas
        String normalizedQuery = normalizeText(query);
        List<Product> baseList;
        if (category != null && !category.isEmpty()) {
            baseList = productRepository.findByCategory(category);
        } else {
            baseList = productRepository.findAll();
        }
        // Filtrar productos disponibles y cuyo nombre normalizado contenga la query normalizada
        return baseList.stream()
                .filter(Product::isAvailable)
                .filter(product -> normalizeText(product.getName()).contains(normalizedQuery))
                .toList();
    }

    // Utilidad para quitar tildes y pasar a minúsculas
    private String normalizeText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
