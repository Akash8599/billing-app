package com.billingsystem.service;

import org.springframework.stereotype.Service;

@Service
public class GSTService {

    /**
     * Calculate tax amount based on price and GST rate
     * Formula: Tax = (Price * GST%) / (100 + GST%)
     */
    public Double calculateTax(Double basePrice, Double gstRate) {
        if (basePrice == null || gstRate == null) {
            return 0.0;
        }
        return (basePrice * gstRate) / (100 + gstRate);
    }

    /**
     * Get final price including tax
     */
    public Double getPriceWithTax(Double basePrice, Double gstRate) {
        return basePrice + calculateTax(basePrice, gstRate);
    }

    /**
     * Determine invoice type based on customer type
     */
    public String getInvoiceType(String customerType) {
        if ("WHOLESALE".equals(customerType)) {
            return "GST_BILL";
        }
        return "SALES";
    }

    /**
     * Validate GST rate (standard rates in India)
     */
    public boolean isValidGSTRate(Double rate) {
        return rate == 5.0 || rate == 12.0 || rate == 18.0 || rate == 28.0;
    }
}
