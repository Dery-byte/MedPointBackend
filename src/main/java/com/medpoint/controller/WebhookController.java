package com.medpoint.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medpoint.dto.paystackdto.PaystackApiResponse;
import com.medpoint.service.PaystackService;
import com.medpoint.webhook.WebhookValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives asynchronous webhook events from Paystack.
 *
 * Register this URL in your Paystack dashboard:
 *   Settings → API Keys & Webhooks → Webhook URL
 *   e.g. https://your-domain.com/api/webhooks/paystack
 *
 * Key events handled:
 *   charge.success – customer has paid successfully
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final ObjectMapper     objectMapper;

    private final WebhookValidator webhookValidator;

    private final PaystackService paystackService;


    @PostMapping("/paystack")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {

        // 1. Always return 200 quickly — Paystack retries if it doesn't get 2xx
        //    Do heavy processing after acknowledging.

        // 2. Verify the request is genuinely from Paystack
        if (!webhookValidator.isValid(rawBody, signature)) {
            log.warn("Invalid webhook signature — ignoring request");
            return ResponseEntity.ok().build(); // still 200 to avoid unnecessary retries
        }

        // 3. Parse payload
        try {
            WebhookPayload payload = objectMapper.readValue(rawBody, WebhookPayload.class);
            log.info("Webhook received | event={}", payload.getEvent());

            // 4. Delegate to service (runs in its own transaction)
            paystackService.handleWebhookEvent(payload.getEvent(), payload.getData());

        } catch (Exception e) {
            log.error("Failed to process webhook payload", e);
            // Still return 200 — we don't want Paystack to retry a malformed payload endlessly
        }

        return ResponseEntity.ok().build();
    }

    /** Internal mapping for the webhook envelope */
    @lombok.Data
    private static class WebhookPayload {
        private String event;
        private PaystackApiResponse.Data data;
    }
}
