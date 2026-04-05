package com.medpoint.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data @Builder
public class DrugResponse {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int stock;
    private LocalDate expiryDate;
    private boolean active;
    private boolean lowStock;
    private String expiryStatus; // OK | EXPIRING_SOON | EXPIRING_IMMINENT | EXPIRED
}
