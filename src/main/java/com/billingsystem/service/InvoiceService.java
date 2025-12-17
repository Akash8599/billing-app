package com.billingsystem.service;


import com.billingsystem.dto.CompanySettingsDTO;
import com.billingsystem.dto.SalesOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class InvoiceService {

    @Autowired
    private CompanySettingsService companySettingsService;

    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * Generate invoice data for a sales order
     */
    public Map<String, Object> generateInvoiceData(Long orderId) {
        log.info("Generating invoice data for order: {}", orderId);

        // Get order details
        SalesOrderResponse order = salesOrderService.getSalesOrderById(orderId);

        // Get company settings
        CompanySettingsDTO companySettings = companySettingsService.getCompanySettings();

        if (companySettings == null) {
            throw new RuntimeException("Company settings not configured");
        }

        // Build invoice data
        Map<String, Object> invoiceData = new HashMap<>();

        // Company Details
        invoiceData.put("companyName", companySettings.getCompanyName());
        invoiceData.put("gstin", companySettings.getGstin());
        invoiceData.put("uin", companySettings.getUin());
        invoiceData.put("address", companySettings.getAddress());
        invoiceData.put("city", companySettings.getCity());
        invoiceData.put("state", companySettings.getState());
        invoiceData.put("postalCode", companySettings.getPostalCode());
        invoiceData.put("contactPhone", companySettings.getContactPhone());
        invoiceData.put("contactEmail", companySettings.getContactEmail());
        invoiceData.put("stateCode", companySettings.getStateCode());
        invoiceData.put("website", companySettings.getWebsite());
        invoiceData.put("logo", companySettings.getLogo());
        invoiceData.put("signature", companySettings.getSignature());
        invoiceData.put("authorizedSignatory", companySettings.getAuthorizedSignatory());
        invoiceData.put("authorizedSignatoryDesignation", companySettings.getAuthorizedSignatoryDesignation());

        // Bank Details
        invoiceData.put("bankName", companySettings.getBankName());
        invoiceData.put("accountNumber", companySettings.getAccountNumber());
        invoiceData.put("ifscCode", companySettings.getIfscCode());
        invoiceData.put("accountHolderName", companySettings.getAccountHolderName());

        // Order Details
        invoiceData.put("invoiceNumber", order.getInvoiceNumber());
        invoiceData.put("orderNumber", order.getOrderNumber());
        invoiceData.put("invoiceDate", formatDate(order.getCreatedAt()));
        invoiceData.put("createdAt", order.getCreatedAt());

        // Customer Details
        invoiceData.put("customerName", order.getCustomerName());
        invoiceData.put("customerPhone", order.getCustomerPhone());
        invoiceData.put("customerEmail", order.getCustomerEmail());
        invoiceData.put("customerAddress", order.getCustomerAddress());
        invoiceData.put("customerGstIn", order.getCustomerGstIn());
        invoiceData.put("customerState", order.getCustomerState());
        invoiceData.put("customerStateCode", order.getCustomerStateCode());

        // Order Items
        invoiceData.put("items", order.getItems());
        invoiceData.put("totalItems", order.getItems() != null ? order.getItems().size() : 0);

        // Financial Details
        invoiceData.put("subtotal", calculateSubtotal(order));
        invoiceData.put("totalTax", order.getTotalTax());
        invoiceData.put("totalAmount", order.getTotalAmount());
        invoiceData.put("paymentStatus", order.getPaymentStatus());

        // Status
        invoiceData.put("status", order.getStatus());

        log.info("✅ Invoice data generated successfully");
        return invoiceData;
    }

    /**
     * Calculate subtotal (total - tax)
     */
    private Double calculateSubtotal(SalesOrderResponse order) {
        if (order.getTotalAmount() == null || order.getTotalTax() == null) {
            return 0.0;
        }
        BigDecimal total = order.getTotalAmount() != null
                ? order.getTotalAmount()
                : BigDecimal.ZERO;

        BigDecimal tax = order.getTotalTax() != null
                ? order.getTotalTax()
                : BigDecimal.ZERO;

        return total.subtract(tax).doubleValue();


    }

    /**
     * Format date
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy");
        return dateTime.format(formatter);
    }
}