package com.billingsystem.dto;

import com.billingsystem.model.SalesOrder;
import jakarta.persistence.Column;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderResponse {

    private Long id;
    private String orderNumber;

    // Customer Info
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
    private String customerStateCode;
    private String customerState;
    private String customerGstIn;

    // Order Items
    private List<SalesOrderItemResponse> items;

    // Amounts
    private BigDecimal subTotal;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;

    // Status
    private String status;
    private String paymentStatus;

    // Invoice
    private String invoiceNumber;
    private LocalDateTime invoiceDate;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;

    // Created by
    private String createdBy;

    // Notes
    private String notes;

    /**
     * Convert SalesOrder entity to response DTO
     */
    public static SalesOrderResponse fromEntity(SalesOrder entity) {
        return SalesOrderResponse.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerEmail(entity.getCustomerEmail())
                .customerAddress(entity.getCustomerAddress())
                .customerGstIn(entity.getCustomerGstIn())
                .customerState(entity.getCustomerState())
                .customerStateCode(entity.getCustomerStateCode())
                .items(entity.getItems() != null ?
                        entity.getItems().stream()
                                .map(SalesOrderItemResponse::fromEntity)
                                .collect(Collectors.toList()) :
                        null)
                .subTotal(entity.getSubTotal())
                .totalTax(entity.getTotalTax())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus() != null ? entity.getStatus().toString() : null)
                .paymentStatus(entity.getPaymentStatus() != null ? entity.getPaymentStatus().toString() : null)
                .invoiceNumber(entity.getInvoiceNumber())
                .invoiceDate(entity.getInvoiceDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .paidAt(entity.getPaidAt())
                .createdBy(entity.getCreatedBy())
                .notes(entity.getNotes())
                .build();
    }
}