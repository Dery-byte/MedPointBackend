package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank private String name;
    @NotBlank private String category;
    @NotNull @DecimalMin("0.00") private BigDecimal price;
    @Min(0) private int stock;
}
