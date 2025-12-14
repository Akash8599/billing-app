package com.billingsystem.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonIgnore
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "id")
    private Product product;

    // Note: productId is automatically mapped by @JoinColumn above
    // We store it separately for convenience
    @Transient  // This field is NOT persisted to database
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_sku")
    private String productSku;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    // Quantity and Pricing
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    // GST
    @Column(name = "gst_rate", nullable = false)
    private Integer gstRate;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax", precision = 10, scale = 2)
    private BigDecimal tax;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    // Discount (optional)
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    // Additional Info
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Calculate subtotal, tax, and total for this item
     */
    public void calculateTotals() {
        // Subtotal = Quantity × Selling Price
        subtotal = sellingPrice.multiply(BigDecimal.valueOf(quantity));

        // Apply discount if any
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100));
            subtotal = subtotal.subtract(discountAmount);
        } else {
            discountAmount = BigDecimal.ZERO;
        }

        // Tax = Subtotal × GST% / 100
        tax = subtotal.multiply(BigDecimal.valueOf(gstRate)).divide(BigDecimal.valueOf(100));

        // Total = Subtotal + Tax
        total = subtotal.add(tax);
    }

    /**
     * Get profit for this item
     */
    public BigDecimal getProfit() {
        if (costPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal costForQuantity = costPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal profitPercentage = ((subtotal.subtract(costForQuantity)).divide(costForQuantity)).multiply(BigDecimal.valueOf(100));
        return profitPercentage;
    }
}