package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** Drugstore consumables that are NOT medicines: gloves, syringes, IV fluids, etc. */
@Entity
@Table(name = "non_drug_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NonDrugItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    /** e.g. Consumable, Fluid, Diagnostic */
    @Column(nullable = false)
    @NotBlank
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
