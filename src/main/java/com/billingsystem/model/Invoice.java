package com.billingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String invoiceNumber;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceItem> items;
    @Column(nullable = false)
    private Double subtotal;
    @Column(nullable = false)
    private Double totalTax;
    @Column(nullable = false)
    private Double totalAmount;
    @Column(nullable = false)
    private String invoiceType;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Long invoiceDate;
    @Column(nullable = false)
    private Long createdAt;
}
