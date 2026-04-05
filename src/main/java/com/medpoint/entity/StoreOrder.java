package com.medpoint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** E-commerce order submitted from the public storefront. */
@Entity
@Table(name = "store_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String customerName;

    private String customerEmail;

    private String phone;

    @Column(nullable = false)
    private String address;

    private String deliveryAddress;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private String paymentMethod;

    private String paymentReference;

    @Column(nullable = false)
    @Builder.Default
    private String status = "pending";

    private String statusChangedBy;

    private LocalDateTime statusChangedAt;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StoreOrderItem> items = new ArrayList<>();
}
