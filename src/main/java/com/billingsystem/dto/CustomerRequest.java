package com.billingsystem.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    private Long id;

    private String name;

    private String phone;

    private String email;

    private String address;

    private String gstin;

    private String customerType;

    private Long createdAt;

    private Long updatedAt;
    private String city;
    private String pincode;
    private String state;
    private String statecode;

}

