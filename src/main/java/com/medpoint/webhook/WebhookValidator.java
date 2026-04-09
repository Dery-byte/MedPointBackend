package com.medpoint.webhook;

import com.medpoint.config.PaystackConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Validates that an incoming webhook actually came from Paystack.
 *
 * Paystack signs every webhook payload with your secret key using HMAC-SHA512
 * and sends the hex digest in the X-Paystack-Signature header.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookValidator {

    private static final String ALGORITHM = "HmacSHA512";

    private final PaystackConfig config;

    public boolean isValid(String rawBody, String signature) {
        if (signature == null || signature.isBlank()) {
            log.warn("Webhook rejected: missing X-Paystack-Signature header");
            return false;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    config.getSecretKey().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(keySpec);

            byte[] hmacBytes = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected  = HexFormat.of().formatHex(hmacBytes);

            boolean valid = expected.equalsIgnoreCase(signature);
            if (!valid) log.warn("Webhook signature mismatch — possible forgery attempt");
            return valid;
        } catch (Exception e) {
            log.error("Error validating webhook signature", e);
            return false;
        }
    }
}
