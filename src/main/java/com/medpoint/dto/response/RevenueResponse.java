package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder
public class RevenueResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal totalProfit;
    private double marginPercent;
    private List<ModuleSummary> byModule;
    private List<StaffSummary> byStaff;
    private List<PeriodSummary> byPeriod;

    @Data @Builder
    public static class ModuleSummary {
        private String module;
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal profit;
        private double marginPercent;
        private int transactionCount;
    }

    @Data @Builder
    public static class StaffSummary {
        private Long staffId;
        private String staffName;
        private BigDecimal revenue;
        private BigDecimal cost;
        private BigDecimal profit;
        private int transactionCount;
    }

    @Data @Builder
    public static class PeriodSummary {
        private String periodKey;
        private String periodLabel;
        private BigDecimal revenue;
        private BigDecimal profit;
        private int transactionCount;
    }
}
