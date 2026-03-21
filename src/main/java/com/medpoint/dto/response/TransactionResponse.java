package com.medpoint.dto.response;
import com.medpoint.enums.LineItemKind;
import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data @Builder
public class TransactionResponse {
    private Long id;
    private String reference;
    private TxModule module;
    private BigDecimal amount;
    private String staffName;
    private String description;
    private TransactionStatus status;
    private List<LineItemDto> lineItems;
    private String cancelledByName;
    private Instant cancelledAt;
    private Instant createdAt;

    @Data @Builder
    public static class LineItemDto {
        private Long id;
        private String name;
        private String category;
        private LineItemKind kind;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
