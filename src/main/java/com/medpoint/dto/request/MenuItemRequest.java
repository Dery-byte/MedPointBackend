package com.medpoint.dto.request;
import com.medpoint.enums.MenuItemType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuItemRequest {
//    @NotBlank private String name;
//    @NotBlank private String category;
//    @NotNull private MenuItemType type;
//    @NotNull @DecimalMin("0.00") private BigDecimal price;

    @NotBlank(message = "Name is required")
    private String name;

    /** Maps to MenuItem.category — called "cat" on the frontend */
    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Type is required")
    private MenuItemType type;   // FOOD | DRINK

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be non-negative")
    private BigDecimal price;


}
