package com.billingsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;
    @Column
    private String email;
    @Column
    private String address;
    @Column(nullable = true)
    private String gstNumber;
    @Column(nullable = true)
    private String customerType;
    @Column(nullable = false)
    private Long createdAt;
    @Column
    private Long updatedAt;
    @Column
    private String stateCode;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String pinCode;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
