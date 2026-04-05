package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "drugs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    /** e.g. Analgesic, Antibiotic, Antimalarial, Supplement */
    @Column(nullable = false)
    @NotBlank
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    @Min(0)
    private int stock;

    /** Optional – used on the Stock page for expiry-filter logic. */
    private LocalDate expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
