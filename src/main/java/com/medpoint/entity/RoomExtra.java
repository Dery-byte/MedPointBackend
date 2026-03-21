package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** Master catalogue of hotel extras (Room Service, Laundry, Spa, etc.). */
@Entity
@Table(name = "room_extras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoomExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) @NotBlank private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Column(nullable = false) @Builder.Default private boolean active = true;
}
