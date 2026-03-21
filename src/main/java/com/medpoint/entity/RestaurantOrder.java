package com.medpoint.entity;

import com.medpoint.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** An open (or closed) order for a restaurant table. */
@Entity
@Table(name = "restaurant_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.OPEN;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant openedAt = Instant.now();

    private Instant billedAt;
}
