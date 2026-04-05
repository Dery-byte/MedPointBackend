package com.medpoint.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReportResponse {

    private BigDecimal totalRevenue;
    private int        totalTransactions;
    private int        cancelledCount;
    private LocalDate  periodFrom;
    private LocalDate  periodTo;

    /** Aggregated rows for the chosen groupBy (by day / month / staff / category / product). */
    private List<GroupRow> groups;

    /** Top 10 line items by revenue — for product/category/inventory views. */
    private List<ItemRow> topItems;

    @Data
    @Builder
    public static class GroupRow {
        /** Human-readable label: date string, staff name, module name, status, etc. */
        private String     label;
        private int        count;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    public static class ItemRow {
        private String     name;
        private String     category;
        private int        totalQty;
        private BigDecimal totalRevenue;
        /** Used for inventory: current stock level */
        private Integer    stock;
    }
}
