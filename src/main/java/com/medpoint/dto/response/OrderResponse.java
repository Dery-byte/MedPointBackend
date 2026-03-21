package com.medpoint.dto.response;
import com.medpoint.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long id;
    private int tableNumber;
    private OrderStatus status;
    private List<OrderItemDto> items;
    private BigDecimal total;
    private Instant openedAt;
    private Instant billedAt;

    @Data @Builder
    public static class OrderItemDto {
        private Long orderItemId;
        private Long menuItemId;
        private String menuItemName;
        private String category;
        private String type;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
