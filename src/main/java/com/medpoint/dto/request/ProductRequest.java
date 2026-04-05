package com.medpoint.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductRequest {
    @NotBlank private String name;
    @NotBlank private String category;
    @NotNull @DecimalMin("0.00") private BigDecimal price;
    private BigDecimal costPrice;
    @Min(0) private int stock;

    // Storefront fields
    private String imageUrl;
    private boolean featured;
    private BigDecimal discount;
    private boolean onSale;
    private boolean showOnStore = true;
    private String description;
    private String tags;
    private String variations;
}
