package com.medpoint.dto.paystackdto;

import com.medpoint.entity.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body your frontend/client sends to POST /api/payments/initialize
 */
@Data
public class InitializePaymentRequest {

    @NotBlank(message = "Customer email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    /**
     * Amount in major units (e.g. 50.00 for GHS 50).
     * The service converts to subunits (pesewas/kobo) before calling Paystack.
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    /** ISO 4217 currency code (e.g. GHS, NGN, USD). Defaults to integration currency. */
    private String currency;

    /** Optional human-readable description */
    private String description;

    /**
     * Optional: override the callback URL set on your Paystack dashboard.
     * Leave null to use application.properties value.
     */
    private String callbackUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)   // change optional to true
    @JoinColumn(name = "staff_id", nullable = true)        // change nullable to true
    private User staff;
}
