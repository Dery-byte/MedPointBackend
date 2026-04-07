package com.medpoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCustomerProfileRequest {
    @NotBlank private String name;
    @NotBlank private String phone;
}
