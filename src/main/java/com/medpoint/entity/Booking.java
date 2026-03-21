package com.medpoint.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Hotel guest check-in record, including extras selected at checkout. */
@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false) @NotBlank private String guestName;
    @Column(nullable = false) @NotBlank private String phone;
    private String nationality;
    private String address;

    /** e.g. National ID, Passport, Driver's License */
    @Column(nullable = false) @NotBlank private String idType;
    @Column(nullable = false) @NotBlank private String idNumber;

    @Column(nullable = false) private LocalDate checkIn;
    @Column(nullable = false) private LocalDate checkOut;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "booking_extras", joinColumns = @JoinColumn(name = "booking_id"))
    @Builder.Default
    private List<BookingExtra> extras = new ArrayList<>();

    @Column(nullable = false) @Builder.Default private boolean paid = false;

    /** Set to true when a Transaction is recorded on checkout. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal totalCharged;
}
