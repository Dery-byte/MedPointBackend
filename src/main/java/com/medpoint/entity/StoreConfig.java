package com.medpoint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StoreConfig {

    /** Always id=1 — singleton row pattern */
    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String configJson;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
