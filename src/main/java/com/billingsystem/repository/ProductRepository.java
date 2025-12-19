package com.billingsystem.repository;

import com.billingsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find product by SKU for a specific user
    Optional<Product> findBySkuAndCreatedBy(String sku, Long createdBy);

    // Find all active products for a specific user
    List<Product> findAllByIsActiveTrueAndCreatedBy(Long createdBy);

    // Find active product by SKU for a specific user
    Optional<Product> findBySkuAndIsActiveTrueAndCreatedBy(String sku, Long createdBy);

    // Find active low stock products for a specific user
    @Query("SELECT p FROM Product p WHERE p.quantity <= p.lowStockAlert AND p.isActive = true AND p.createdBy = :createdBy")
    List<Product> findLowStockProductsActiveByUser(Long createdBy);

    // Get product by ID for a specific user
    Optional<Product> findByIdAndCreatedBy(Long id, Long createdBy);
}
