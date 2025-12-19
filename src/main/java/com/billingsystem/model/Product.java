package com.billingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double costPrice;

    @Column(nullable = false)
    private Double sellingPrice;

    @Column(nullable = false)
    private Double gstRate;

    @Column(nullable = false)
    private Integer lowStockAlert;

    // ════════════════════════════════════════════════════════════════════════
    // ✅ NEW: Is Active (Soft Delete)
    // ════════════════════════════════════════════════════════════════════════
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(nullable = false)
    private Long createdAt;

    @Column
    private Long updatedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}