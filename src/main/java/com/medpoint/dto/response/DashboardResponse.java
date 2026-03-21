package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Dashboard response — fields match frontend AdminDashboard.jsx exactly:
 *   dash.todayRevenue, dash.totalRevenue, dash.activeStaff,
 *   dash.lowStockCount, dash.revenueByModule, dash.recentTransactions
 */
@Data @Builder
public class DashboardResponse {
    private BigDecimal todayRevenue;
    private BigDecimal totalRevenue;
    private long activeStaff;
    private int lowStockCount;
    private Map<String, BigDecimal> revenueByModule;
    private List<RecentTx> recentTransactions;

    @Data @Builder
    public static class RecentTx {
        private Long id;
        private String staff;       // frontend: t.staff
        private String module;
        private BigDecimal amount;
        private String description;
        private String status;
        private String date;
    }
}
