package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class StockAlertResponse {
    private List<DrugAlert> lowStockDrugs;
    private List<DrugAlert> expiringDrugs;
    private List<ProductAlert> lowStockProducts;

    @Data @Builder
    public static class DrugAlert {
        private Long id;
        private String name;
        private String category;
        private int stock;
        private BigDecimal price;
        private LocalDate expiryDate;
        private String expiryStatus;
    }

    @Data @Builder
    public static class ProductAlert {
        private Long id;
        private String name;
        private String category;
        private int stock;
        private BigDecimal price;
    }
}
