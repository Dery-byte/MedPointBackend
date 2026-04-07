package com.medpoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SaveConfigProfileRequest {
    @NotBlank
    private String name;
    private String modulesJson;
    private String themePreset;
}
