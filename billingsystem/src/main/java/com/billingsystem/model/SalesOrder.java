package com.billingsystem.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    // Customer Information
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    // Order Details
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "salesOrder")
    private List<SalesOrderItem> items;

    // Amounts
    @Column(name = "sub_total", precision = 10, scale = 2)
    private BigDecimal subTotal;

    @Column(name = "total_tax", precision = 10, scale = 2)
    private BigDecimal totalTax;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    // Payment Status
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    // Invoice
    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // User who created the order
    @Column(name = "created_by")
    private String createdBy;

    // Notes
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = OrderStatus.CREATED;
        paymentStatus = PaymentStatus.UNPAID;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum OrderStatus {
        CREATED,      // Order just created
        CONFIRMED,    // Order confirmed by admin
        PROCESSING,   // Order being processed
        SHIPPED,      // Order shipped
        DELIVERED,    // Order delivered
        CANCELLED     // Order cancelled
    }

    public enum PaymentStatus {
        UNPAID,       // Not paid yet
        PARTIALLY_PAID, // Partial payment
        PAID,         // Fully paid
        REFUNDED      // Refunded
    }

    /**
     * Calculate and set the total amount including tax
     */
    public void calculateTotals() {
        if (items == null || items.isEmpty()) {
            subTotal = BigDecimal.ZERO;
            totalTax = BigDecimal.ZERO;
            totalAmount = BigDecimal.ZERO;
            return;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        for (SalesOrderItem item : items) {
            BigDecimal itemSubtotal = item.getSubtotal();
            BigDecimal itemTax = item.getTax();

            subtotal = subtotal.add(itemSubtotal);
            tax = tax.add(itemTax);
        }

        this.subTotal = subtotal;
        this.totalTax = tax;
        this.totalAmount = subtotal.add(tax);
    }
}