package com.medpoint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable audit record for every status transition on a store order.
 * A new row is written each time {@code StoreOrder.status} changes.
 */
@Entity
@Table(name = "store_order_status_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreOrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StoreOrder order;

    /** The status before this change (null when the order is first created). */
    private String fromStatus;

    @Column(nullable = false)
    private String toStatus;

    /** Display name of the staff member or system actor who made the change. */
    private String changedBy;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();
}
