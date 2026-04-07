package com.medpoint.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class StoreOrderResponse {
    private Long id;
    private String reference;
    private String status;
    private String statusChangedBy;
    private LocalDateTime statusChangedAt;
    private String customerName;
    private String customerEmail;
    private String phone;
    private String address;
    private BigDecimal total;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<Item> items;

    /** Full status-change audit trail — populated for admin views, null for customer views. */
    private List<StatusHistoryEntry> statusHistory;

    @Data @Builder
    public static class Item {
        private Long productId;
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String imageUrl;
    }

    @Data @Builder
    public static class StatusHistoryEntry {
        private String fromStatus;
        private String toStatus;
        private String changedBy;
        private LocalDateTime changedAt;
    }
}
