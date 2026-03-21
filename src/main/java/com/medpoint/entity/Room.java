package com.medpoint.entity;

import com.medpoint.enums.RoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/** A physical hotel room. */
@Entity
@Table(name = "rooms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable room number, e.g. "101", "301". */
    @Column(nullable = false, unique = true)
    @NotBlank
    private String roomNumber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private RoomCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;
}
