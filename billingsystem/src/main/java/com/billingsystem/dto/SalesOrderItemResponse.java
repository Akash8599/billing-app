package com.billingsystem.dto;

import com.billingsystem.model.SalesOrderItem;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productDescription;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private Integer gstRate;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private String notes;

    /**
     * Convert SalesOrderItem entity to response DTO
     */
    public static SalesOrderItemResponse fromEntity(SalesOrderItem entity) {
        if (entity == null) {
            return null;
        }

        return SalesOrderItemResponse.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .productSku(entity.getProductSku())
                .productDescription(entity.getProductDescription())
                .quantity(entity.getQuantity())
                .sellingPrice(entity.getSellingPrice())
                .costPrice(entity.getCostPrice())
                .gstRate(entity.getGstRate())
                .subtotal(entity.getSubtotal())
                .tax(entity.getTax())
                .total(entity.getTotal())
                .discountPercent(entity.getDiscountPercent())
                .discountAmount(entity.getDiscountAmount())
                .notes(entity.getNotes())
                .build();
    }

    /**
     * Convert response DTO back to entity (if needed)
     */
    public SalesOrderItem toEntity() {
        return SalesOrderItem.builder()
                .id(this.id)
                .productId(this.productId)
                .productName(this.productName)
                .productSku(this.productSku)
                .productDescription(this.productDescription)
                .quantity(this.quantity)
                .sellingPrice(this.sellingPrice)
                .costPrice(this.costPrice)
                .gstRate(this.gstRate)
                .subtotal(this.subtotal)
                .tax(this.tax)
                .total(this.total)
                .discountPercent(this.discountPercent)
                .discountAmount(this.discountAmount)
                .notes(this.notes)
                .build();
    }
}