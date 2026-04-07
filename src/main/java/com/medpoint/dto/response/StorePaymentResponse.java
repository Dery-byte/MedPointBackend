package com.medpoint.dto.response;

import com.medpoint.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StorePaymentResponse {
    private boolean success;
    private String message;
    private String reference;
    private String authorizationUrl;   // redirect the customer here
    private String accessCode;         // use with Paystack Popup JS
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    public static StorePaymentResponse from(Transaction tx, String message) {
        return StorePaymentResponse.builder()
                .success(true)
                .message(message)
                .reference(tx.getReference())
                .authorizationUrl(tx.getAuthorizationUrl())
                .accessCode(tx.getAccessCode())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus().name())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public static StorePaymentResponse error(String message) {
        return StorePaymentResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
