package com.medpoint.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DiscountDTO {
    private String type;
    private BigDecimal value;
    private String label;
    private LocalDateTime endsAt;
}