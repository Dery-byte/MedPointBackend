package com.medpoint.dto.paystackdto;

import com.medpoint.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Unified response returned to clients for any payment operation.
 */
@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private String message;
    private String reference;
    private String authorizationUrl;   // redirect the customer here
    private String accessCode;         // use with Paystack Popup JS
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Transaction tx, String message) {
        return PaymentResponse.builder()
                .success(true)
                .message(message)
                .reference(tx.getReference())
                .authorizationUrl(tx.getAuthorizationUrl())
                .accessCode(tx.getAccessCode())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus().name())
                .createdAt(LocalDateTime.from(tx.getCreatedAt()))
                .build();
    }

    public static PaymentResponse error(String message) {
        return PaymentResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
