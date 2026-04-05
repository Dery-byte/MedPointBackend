package com.medpoint.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ConfigProfileResponse {
    private Long id;
    private String name;
    private String modulesJson;
    private String themePreset;
    private LocalDateTime savedAt;
}
