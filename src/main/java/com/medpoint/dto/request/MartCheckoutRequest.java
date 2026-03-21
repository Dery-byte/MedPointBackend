package com.medpoint.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class MartCheckoutRequest {
    @NotEmpty private List<CartItem> items;

    @Data
    public static class CartItem {
        @NotNull private Long productId;
        @Min(1) private int quantity;
    }
}
