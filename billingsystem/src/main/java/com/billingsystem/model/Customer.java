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
    @Column
    private String phoneNumber;
    @Column
    private String email;
    @Column
    private String address;
    @Column(nullable = false)
    private String gstNumber;
    @Column(nullable = false)
    private String customerType;
    @Column(nullable = false)
    private Long createdAt;
    @Column(nullable = false)
    private Long updatedAt;
}
