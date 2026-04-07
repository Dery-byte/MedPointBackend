// dto/response/ServiceReceiptResponse.java
package com.medpoint.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ServiceReceiptResponse(
        Long            transactionId,
        String          reference,
        BigDecimal      svcTotal,
        BigDecimal      itemTotal,
        BigDecimal      grandTotal,
        java.time.LocalDateTime issuedAt,
        List<LineDto>   lineItems
) {
    public record LineDto(
            String     name,
            String     cat,
            String     kind,
            int        qty,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}