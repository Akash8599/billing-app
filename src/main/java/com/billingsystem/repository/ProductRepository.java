package com.billingsystem.repository;

import com.billingsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.lowStockAlert")
    List<Product> findLowStockProducts();

    // ════════════════════════════════════════════════════════════════════════
    // ✅ NEW: Only return active products
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Get all ACTIVE products only
     * Soft deleted products (isActive = false) are excluded
     */
    List<Product> findAllByIsActiveTrue();

    /**
     * Get active product by SKU
     */
    Optional<Product> findBySkuAndIsActiveTrue(String sku);

    /**
     * Get active low stock products
     */
    @Query("SELECT p FROM Product p WHERE p.quantity <= p.lowStockAlert AND p.isActive = true")
    List<Product> findLowStockProductsActive();

    /**
     * Get product by ID (can be active or inactive)
     * Used internally to check if product exists
     */
    Optional<Product> findById(Long id);
}