package com.rotacerta.api.repository;

import com.rotacerta.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCase(String sku);
}
