package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** Hotel room category (Standard / Deluxe / Suite) with price-per-night. */
@Entity
@Table(name = "room_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal pricePerNight;
}
