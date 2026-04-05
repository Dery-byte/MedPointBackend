package com.medpoint.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddBookingExtrasRequest {
    @NotEmpty
    private List<Long> extraIds;
}
