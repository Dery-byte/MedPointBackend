package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RoomCategoryRequest {
//    @NotBlank private String name;
//    @NotNull @DecimalMin("0.00") private BigDecimal pricePerNight;


    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal pricePerNight;
}
