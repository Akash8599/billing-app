package com.billingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Double todaysSales;
    private Integer totalInvoices;
    private Integer lowStockCount;
    private Double totalRevenue;
    private List<LowStockItem> lowStockItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockItem {
        private Long productId;
        private String sku;
        private String name;
        private Integer currentQuantity;
        private Integer alertLevel;
    }
}
