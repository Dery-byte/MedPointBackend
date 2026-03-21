package com.medpoint.entity;

import com.medpoint.enums.LineItemKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/** One line item within a Transaction – price/name/qty snapshot at sale time. */
@Entity
@Table(name = "transaction_line_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false) @NotBlank private String name;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LineItemKind kind = LineItemKind.ITEM;

    @Column(nullable = false) @Min(1) private int quantity;

    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal unitPrice;

    /** Stored subtotal = unitPrice * quantity (avoids repeated computation in reports). */
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal subtotal;
}
