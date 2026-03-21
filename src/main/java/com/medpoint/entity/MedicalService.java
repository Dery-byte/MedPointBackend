package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** Drugstore clinical services: Consultation, Diagnostic, Treatment. */
@Entity
@Table(name = "medical_services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    /** e.g. Consultation, Diagnostic, Treatment */
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
