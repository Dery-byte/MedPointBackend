package com.medpoint.service;

import com.medpoint.dto.paystackdto.InitializePaymentRequest;
import com.medpoint.dto.paystackdto.PaymentResponse;
import com.medpoint.dto.paystackdto.PaystackApiResponse;
import com.medpoint.dto.response.StorePaymentResponse;

public interface PaystackService {

    /**
     * Initialize a new payment transaction with Paystack.
     * Returns an authorizationUrl to redirect the customer to,
     * and an accessCode for Paystack Popup JS.
     */
    StorePaymentResponse initializePayment(InitializePaymentRequest request);

    /**
     * Verify the status of a transaction by its reference.
     * Should be called from your callback URL or on demand.
     */
    StorePaymentResponse verifyTransaction(String reference);

    /**
     * Handle an incoming webhook event from Paystack.
     * Currently processes charge.success events.
     */
    void handleWebhookEvent(String event, PaystackApiResponse.Data data);

}
