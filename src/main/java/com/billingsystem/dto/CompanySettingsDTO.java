package com.billingsystem.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettingsDTO {

    // Company Details
    private String companyName;
    private String gstin;
    private String uin;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String contactPhone;
    private String contactEmail;

    // Bank Details
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;

    // Additional Info
    private String stateCode;
    private String website;
    private String logo; // Base64
    private String signature; // Base64
    private String authorizedSignatory;
    private String authorizedSignatoryDesignation;
}