package com.medpoint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_profiles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfigProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** JSON array of module keys e.g. ["drugstore","mart"] */
    @Column(columnDefinition = "TEXT")
    private String modulesJson;

    private String themePreset;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime savedAt = LocalDateTime.now();
}
