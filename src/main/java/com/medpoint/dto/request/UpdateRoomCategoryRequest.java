package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

/** Used to update price only (name is immutable after creation). */
@Data
public class UpdateRoomCategoryRequest {
    @NotNull @DecimalMin("0.00") private BigDecimal pricePerNight;
}
