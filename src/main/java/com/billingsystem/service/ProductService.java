package com.billingsystem.service;

import com.billingsystem.model.Product;
import com.billingsystem.model.SalesOrderItem;
import com.billingsystem.repository.ProductRepository;
import com.billingsystem.repository.SalesOrderItemRepository;
import com.billingsystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final GSTService gstService;

    /**
     * Create new product
     */
    public Product createProduct(Product product) {
        if (!gstService.isValidGSTRate(product.getGstRate())) {
            throw new RuntimeException("Invalid GST rate. Use: 5, 12, 18, or 28");
        }

        product.setCreatedAt(System.currentTimeMillis());
        // product.setUpdatedAt(System.currentTimeMillis());
        product.setCreatedBy(SecurityUtils.currentUserId());
        product.setIsActive(true); // ← Ensure new product is active
        return productRepository.save(product);
    }

    /**
     * Get all ACTIVE products only
     */
    public List<Product> getAllProducts() {
        return productRepository.findAllByIsActiveTrueAndCreatedBy(SecurityUtils.currentUserId()); // ← Only active
    }

    /**
     * Get product by ID (can be active or inactive)
     * Used for internal operations
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by SKU (only active)
     */
    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySkuAndCreatedBy(sku, SecurityUtils.currentUserId());
    }

    /**
     * Update product details
     */
    public Product updateProduct(Long id, Product productData) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!gstService.isValidGSTRate(productData.getGstRate())) {
            throw new RuntimeException("Invalid GST rate");
        }

        product.setName(productData.getName());
        product.setDescription(productData.getDescription());
        product.setCostPrice(productData.getCostPrice());
        product.setSellingPrice(productData.getSellingPrice());
        product.setGstRate(productData.getGstRate());
        product.setLowStockAlert(productData.getLowStockAlert());
        product.setQuantity(productData.getQuantity());
        product.setUpdatedAt(System.currentTimeMillis());
        product.setUpdatedBy(SecurityUtils.currentUserId());

        return productRepository.save(product);
    }

    /**
     * Get low stock products (only active)
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProductsActiveByUser(SecurityUtils.currentUserId());
    }

    /**
     * Update product quantity
     */
    public void updateQuantity(Long productId, int newQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setQuantity(newQuantity);
        product.setUpdatedAt(System.currentTimeMillis());
        productRepository.save(product);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ✅ SMART DELETE - Soft Delete Strategy
    // ════════════════════════════════════════════════════════════════════════

    /**
     * ✅ DELETE PRODUCT - Smart Soft Delete
     *
     * Logic:
     * 1. If product is used in sales orders:
     * → Mark as isActive = false (soft delete)
     * → Old orders keep snapshot data, no issues
     * → New orders won't see this product
     *
     * 2. If product is NOT used:
     * → Actually delete from database (hard delete)
     * → No orphaned data since not used anywhere
     *
     * Benefits:
     * ✅ No data loss
     * ✅ Old orders unaffected (have snapshot)
     * ✅ Clean database (unused products removed)
     * ✅ New orders don't show deleted product
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check if product is used in any sales orders
        List<SalesOrderItem> usedInOrders = salesOrderItemRepository.findByProductIdAndUser(id,
                SecurityUtils.currentUserId());

        if (usedInOrders != null && !usedInOrders.isEmpty()) {
            // ════════════════════════════════════════════════════════════════
            // PRODUCT IS USED: Mark as inactive (Soft Delete)
            // ════════════════════════════════════════════════════════════════

            log.warn("Product '{}' is used in {} sales order(s). Action: Marking as inactive (Soft Delete)",
                    product.getName(), usedInOrders.size());
            log.debug("Soft delete rationale: Old orders have snapshot, no constraint violations.");

            // Mark as inactive
            product.setIsActive(false);
            product.setUpdatedAt(System.currentTimeMillis());
            productRepository.save(product);

            log.info("Product '{}' marked as INACTIVE (soft deleted). Hidden from new orders.", product.getName());

        } else {
            // ════════════════════════════════════════════════════════════════
            // PRODUCT IS NOT USED: Delete it permanently (Hard Delete)
            // ════════════════════════════════════════════════════════════════

            log.info("Product '{}' is NOT used in any orders. Action: Deleting permanently (Hard Delete)",
                    product.getName());

            try {
                productRepository.deleteById(id);
                log.info("Product '{}' DELETED from database.", product.getName());

            } catch (Exception e) {
                log.error("Error deleting product '{}': {}", product.getName(), e.getMessage());
                throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Get product usage count
     */
    public long getProductUsageCount(Long productId) {
        List<SalesOrderItem> items = salesOrderItemRepository.findByProductIdAndUser(productId,
                SecurityUtils.currentUserId());
        return items != null ? items.size() : 0;
    }

    /**
     * Check if product is used in orders
     */
    public boolean isProductUsedInOrders(Long productId) {
        List<SalesOrderItem> items = salesOrderItemRepository.findByProductIdAndUser(productId,
                SecurityUtils.currentUserId());
        return items != null && !items.isEmpty();
    }

    /**
     * Reactivate a soft-deleted product
     */
    public Product reactivateProduct(Long productId) {
        Product product = productRepository.findByIdAndCreatedBy(productId, SecurityUtils.currentUserId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getIsActive()) {
            throw new RuntimeException("Product is already active");
        }

        product.setIsActive(true);
        product.setUpdatedAt(System.currentTimeMillis());
        return productRepository.save(product);
    }

    /**
     * Get all products (including inactive) - For admin only
     */
    public List<Product> getAllProductsIncludingInactive() {
        return productRepository.findAll();
    }
}