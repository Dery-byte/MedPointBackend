package com.medpoint.controller;
import com.medpoint.dto.paystackdto.InitializePaymentRequest;
import com.medpoint.dto.response.StorePaymentResponse;
import com.medpoint.service.PaystackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST endpoints consumed by your frontend or mobile app.
 *
 * Typical flow:
 *   1. POST /api/payments/initialize      → get authorizationUrl / accessCode
 *   2. Redirect user to authorizationUrl  (or use Popup JS with accessCode)
 *   3. User pays, Paystack calls our webhook (/api/webhooks/paystack)
 *   4. GET  /api/payments/verify/{ref}    → optionally confirm status from callback
 *   5. GET  /api/payments/{ref}           → query transaction details
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaystackService paystackService;
    /**
     * Initialize a new payment transaction.
     * Returns authorizationUrl to redirect the customer to, plus accessCode for Popup JS.
     */



    @Value("${app.front-end}")
    private String frontendUrl;

    @PostMapping("/initialize")
    public ResponseEntity<StorePaymentResponse> initialize(
            @Valid @RequestBody InitializePaymentRequest request) {
        log.info("Initialize payment request | email={} amount={}", request.getEmail(), request.getAmount());
        StorePaymentResponse response = paystackService.initializePayment(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Verify a transaction by reference.
     * Called from your callback URL after Paystack redirects the customer back.
     *
     * Paystack appends ?reference=xxx to your callback URL — read it here.
     */
    @GetMapping("/verify/{reference}")
    public ResponseEntity<StorePaymentResponse> verify(@PathVariable String reference) {
        log.info("Verify transaction | ref={}", reference);
        StorePaymentResponse response = paystackService.verifyTransaction(reference);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Paystack GET callback — Paystack redirects the browser here after payment.
     * The reference comes as a query param: /api/payments/callback?reference=TXN-XXX
     */
//    @GetMapping("/callback")
//    public ResponseEntity<StorePaymentResponse> callback(@RequestParam String reference) {
//        log.info("Paystack callback received | ref={}", reference);
//        StorePaymentResponse response = paystackService.verifyTransaction(reference);
//        // In a real app you'd redirect to your frontend success/failure page here
//        return ResponseEntity.ok(response);
//    }


    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String reference) {
        log.info("Paystack callback received | ref={}", reference);
        paystackService.verifyTransaction(reference);
        // Redirect to frontend success page
        HttpHeaders headers = new HttpHeaders();
        String redirectUrl = frontendUrl + "/order-confirmation?reference=" + reference;
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
