package com.medpoint.dto.paystackdto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Mirrors the standard Paystack API response envelope:
 * { "status": true, "message": "...", "data": { ... } }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaystackApiResponse {

    private boolean status;
    private String message;
    private Data data;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        // ── Initialize transaction fields ────────────────────────────────
        @JsonProperty("authorization_url")
        private String authorizationUrl;

        @JsonProperty("access_code")
        private String accessCode;

        private String reference;

        // ── Verify transaction fields ────────────────────────────────────
        private String status;    // "success" | "failed" | "abandoned" | "pending"
        private BigDecimal amount; // in subunits
        private String currency;
        private String channel;

        @JsonProperty("gateway_response")
        private String gatewayResponse;

        @JsonProperty("paid_at")
        private String paidAt;

        private Long id; // Paystack's internal transaction ID

        private Customer customer;

        private Authorization authorization;

        // ── Shared ────────────────────────────────────────────────────────
        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Customer {
            private Long id;
            private String email;
            @JsonProperty("first_name") private String firstName;
            @JsonProperty("last_name")  private String lastName;
            @JsonProperty("customer_code") private String customerCode;
        }

        @lombok.Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Authorization {
            @JsonProperty("authorization_code") private String authorizationCode;
            private String channel;
            private String bin;
            private String last4;
            @JsonProperty("exp_month") private String expMonth;
            @JsonProperty("exp_year")  private String expYear;
            @JsonProperty("card_type") private String cardType;
            private String bank;
            private String brand;
            private Boolean reusable;
        }
    }
}
