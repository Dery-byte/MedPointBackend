package com.medpoint.entity;

import com.medpoint.enums.MenuItemType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** A restaurant menu item (food or drink). */
@Entity
@Table(name = "menu_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) @NotBlank private String name;

    /** e.g. Starters, Main Course, Soft Drinks, Alcoholic, Hot Drinks */
    @Column(nullable = false) @NotBlank private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemType type;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Column(nullable = false) @Builder.Default private boolean active = true;
}
