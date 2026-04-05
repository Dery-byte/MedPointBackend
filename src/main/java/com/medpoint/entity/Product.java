package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** Mart product (groceries, beverages, household, electronics, etc.). */
@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;




    @Column(nullable = true, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal costPrice;

    @Column(nullable = false)
    @Min(0)
    private int stock;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = true)
    private String imageUrl;

    private String thumbnailUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean featured = false;

    @Column(nullable = true, precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false)
    @Builder.Default
    private boolean onSale = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean showOnStore = true;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    /** Comma-separated tags or JSON string for storefront filtering */
    @Column(nullable = true)
    private String tags;

    /** JSON string: [{name, options:[{label, price, stock, colorHex}]}] */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String variations;
}
