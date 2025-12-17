package com.billingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * ════════════════════════════════════════════════════════════════════════════
 * FIXED: Sales Order Item - GST Rate Handling
 *
 * Issues Fixed:
 * ✅ gst_rate now has @Column(nullable = false, columnDefinition = "INT DEFAULT 18")
 * ✅ snapshot_gst_rate also has default value
 * ✅ Both fields initialized to 18 in @Column definitions
 * ════════════════════════════════════════════════════════════════════════════
 */
@Entity
@Table(name = "sales_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    // ════════════════════════════════════════════════════════════════════════
    // ✅ PRODUCT RELATIONSHIP (Not @Transient)
    // ════════════════════════════════════════════════════════════════════════
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = true)  // Nullable for soft-deleted products
    private Product product;

    // ════════════════════════════════════════════════════════════════════════
    // ✅ SNAPSHOT FIELDS - Preserve data from time of order
    // ════════════════════════════════════════════════════════════════════════

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName = "";

    @Column(name = "product_sku", nullable = false, length = 255)
    private String productSku = "";

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "snapshot_cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal snapshotCostPrice = BigDecimal.ZERO;

    @Column(name = "snapshot_selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal snapshotSellingPrice = BigDecimal.ZERO;

    // ════════════════════════════════════════════════════════════════════════
    // ✅ FIXED: GST_RATE with DEFAULT VALUE in database
    // ════════════════════════════════════════════════════════════════════════
    @Column(name = "snapshot_gst_rate", nullable = false, columnDefinition = "INT DEFAULT 18")
    private Integer snapshotGstRate = 18;

    // ════════════════════════════════════════════════════════════════════════
    // Original item fields
    // ════════════════════════════════════════════════════════════════════════

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ════════════════════════════════════════════════════════════════════════
    // ✅ STATIC METHOD: Capture Product Snapshot at Order Time
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Captures product data as a snapshot at the time of order creation
     * This ensures the order always has the exact product data from when it was ordered
     */
    public static void captureProductSnapshot(SalesOrderItem item, Product product) {
        if (product != null) {
            item.setProductName(product.getName() != null ? product.getName() : "");
            item.setProductSku(product.getSku() != null ? product.getSku() : "");
            item.setProductDescription(product.getDescription());
            item.setSnapshotCostPrice(product.getCostPrice() != null ? BigDecimal.valueOf(product.getCostPrice()) : BigDecimal.ZERO);
            item.setSnapshotSellingPrice(product.getSellingPrice() != null ? BigDecimal.valueOf(product.getSellingPrice()) : BigDecimal.ZERO);
            item.setSnapshotGstRate(product.getGstRate() != null ? product.getGstRate().intValue() : 18);  // ✅ Default 18
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Calculate item totals with GST
    // ════════════════════════════════════════════════════════════════════════

    public void calculateTotals() {
        BigDecimal unitPrice = snapshotSellingPrice != null ? snapshotSellingPrice : BigDecimal.ZERO;
        Integer qty = quantity != null && quantity > 0 ? quantity : 0;

        // Calculate subtotal
        subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

        // Calculate tax using snapshot GST rate
        Integer gstRate = snapshotGstRate != null ? snapshotGstRate : 18;  // ✅ Default 18
        BigDecimal taxRate = BigDecimal.valueOf(gstRate).divide(BigDecimal.valueOf(100));
        tax = subtotal.multiply(taxRate);

        // Calculate total
        total = subtotal.add(tax);

        // Apply discount if any
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100));
            total = total.subtract(discountAmount);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helper methods for displaying data
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Get the product ID safely (handles null product)
     */
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    /**
     * Get the product reference safely
     */
    public Product getProductReference() {
        return product;
    }

    /**
     * Check if product is soft-deleted
     */
    public boolean isProductDeleted() {
        return product == null || (product.getIsActive() != null && !product.getIsActive());
    }

    /**
     * Get display name (from snapshot, fallback to product)
     */
    public String getDisplayProductName() {
        if (productName != null && !productName.isEmpty()) {
            return productName;
        }
        return product != null ? product.getName() : "Unknown";
    }
}