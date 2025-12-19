package com.billingsystem.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Company Details
    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String gstin;

    @Column
    private String uin;

    @Column(nullable = false)
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String postalCode;

    @Column
    private String contactPhone;

    @Column
    private String contactEmail;

    // Bank Details
    @Column
    private String bankName;

    @Column
    private String accountNumber;

    @Column
    private String ifscCode;

    @Column
    private String accountHolderName;

    // Additional Info
    @Column
    private String stateCode;

    @Column
    private String website;


    @Column(columnDefinition = "TEXT")
    private String logo; // Base64 encoded logo

    @Column(columnDefinition = "TEXT")
    private String signature; // Base64 encoded signature

    @Column
    private String authorizedSignatory;

    @Column
    private String authorizedSignatoryDesignation;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;


    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}