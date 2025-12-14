package com.billingsystem.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSalesOrderRequest {

    // Customer Information
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;

    // Order Items
    private List<SalesOrderItemRequest> items;

    // Optional
    private String notes;

    /**
     * Inner DTO for order items
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesOrderItemRequest {
        private Long productId;
        private Integer quantity;
        private java.math.BigDecimal sellingPrice;
        private Integer gstRate; // Optional, will use product's GST if not provided
        private java.math.BigDecimal discountPercent;
    }
}