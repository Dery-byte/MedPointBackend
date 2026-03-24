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




    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal costPrice;

    @Column(nullable = false)
    @Min(0)
    private int stock;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
