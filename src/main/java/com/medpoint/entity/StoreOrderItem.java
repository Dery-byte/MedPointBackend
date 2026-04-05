package com.medpoint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Line item in a storefront order. */
@Entity
@Table(name = "store_order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StoreOrder order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    private String imageUrl;
}
