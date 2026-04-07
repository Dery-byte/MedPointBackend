package com.medpoint.entity;

import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A completed sale or service across any module. */
@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable reference: TX-1001, TX-1002, … */
    @Column(nullable = false, unique = true)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TxModule module;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "staff_id", nullable = true)  // change nullable to true
    private User staff;


    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionLineItem> lineItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_id")
    private User cancelledBy;

    private Instant cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;



    /** Customer email */
    @Column(nullable = false)
    private String email;

    /**
     * Amount in MAJOR currency units (e.g. GHS 50.00).
     * We multiply by 100 before sending to Paystack (subunits).
     */


    /** ISO 4217 currency code — defaults to your integration currency */
    @Column(length = 10)
    private String currency;

    /**
     * Transaction status:
     *   PENDING  – initialized, awaiting payment
     *   SUCCESS  – confirmed via webhook or verify endpoint
     *   FAILED   – payment failed
     *   ABANDONED – customer closed checkout
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OnlineTransactionStatus onlineStatus;

    /** Paystack authorization URL returned on initialization */
    @Column(length = 512)
    private String authorizationUrl;

    /** Paystack access code for Popup JS flow */
    @Column(length = 100)
    private String accessCode;

    /** Optional: store Paystack's internal transaction ID */
    private Long paystackId;

    /** Authorization code for recurring charges (stored after first success) */
    @Column(length = 100)
    private String authorizationCode;



    /** Optional metadata (stored as JSON string) */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt =  LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OnlineTransactionStatus {
        PENDING, SUCCESS, FAILED, ABANDONED
    }
}
