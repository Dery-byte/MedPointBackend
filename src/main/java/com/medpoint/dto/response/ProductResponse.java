package com.medpoint.dto.response;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int stock;
    private boolean active;
    private boolean lowStock;
}
