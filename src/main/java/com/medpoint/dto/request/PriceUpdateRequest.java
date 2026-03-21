package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceUpdateRequest {
    @NotNull @DecimalMin("0.00") private BigDecimal price;
}
