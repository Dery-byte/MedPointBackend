package com.medpoint.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DevAuthRequest {
    @NotBlank @Email
    private String email;
}
