package com.medpoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StoreOrderRequest {

    @NotBlank private String customerName;
    private String customerEmail;
    private String phone;
    @NotBlank private String address;
    private String deliveryAddress;

    @NotEmpty private List<OrderItem> items;

    @NotNull private BigDecimal total;
    @NotBlank private String paymentMethod;
    private String paymentReference;

    @Data
    public static class OrderItem {
        @NotNull private Long productId;
        @NotBlank private String name;
        private int quantity;
        @NotNull private BigDecimal unitPrice;
        private String imageUrl;
    }
}
