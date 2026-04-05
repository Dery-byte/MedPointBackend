package com.medpoint.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateBookingRequest {
    @NotNull
    private LocalDate checkOut;
}
