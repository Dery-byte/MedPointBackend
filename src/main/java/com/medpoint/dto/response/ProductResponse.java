package com.medpoint.dto.response;

import com.medpoint.enums.DiscountType;
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

    // Storefront fields
    private String imageUrl;
    private boolean featured;
    private BigDecimal discount;
    private DiscountType discountType;
    private boolean onSale;
    private boolean showOnStore;
    private String description;
    private String tags;
    private String variations;
}
