package com.billingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String poNumber;
    @Column(nullable = false)
    private String supplierName;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderItem> items;
    @Column(nullable = false)
    private Double totalAmount;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Long orderDate;
    @Column
    private Long receivedDate;
    @Column(nullable = false)
    private Long createdAt;

    @Column
    private Long updatedAt;
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
