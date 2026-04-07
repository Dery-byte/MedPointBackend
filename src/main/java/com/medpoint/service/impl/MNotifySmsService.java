package com.medpoint.service.impl;

import com.medpoint.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Sends SMS via the MNotify Quick SMS API.
 * API docs: https://apps.mnotify.net/docs
 *
 * Replace mnotify.api-key and mnotify.sender-id in application.yml with real credentials.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MNotifySmsService implements SmsService {

    private static final String API_URL = "https://api.mnotify.com/api/sms/quick";

    private final RestTemplate restTemplate;

    @Value("${mnotify.v2.key}")
    private String apiKey;

    @Value("${mnotify.v2.sender-id}")
    private String senderId;

    @Override
    public void send(String phone, String message) {
        if (phone == null || phone.isBlank()) {
            log.warn("SMS skipped — no phone number provided");
            return;
        }

        String internationalPhone = toInternational(phone.trim());

        // Pass API key as query parameter, not a header
        String urlWithKey = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("key", apiKey)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Remove: headers.set("key", apiKey);

        Map<String, Object> body = Map.of(
                "recipient",     List.of(internationalPhone),
                "sender",        senderId,
                "message",       message,
                "is_schedule",   false,
                "schedule_date", ""
        );

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    urlWithKey,        // <-- use the URL with key appended
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            log.info("MNotify SMS sent to {} — status {}", internationalPhone, response.getStatusCode());
        } catch (Exception ex) {
            log.error("MNotify SMS failed for {}: {}", internationalPhone, ex.getMessage());
        }
    }
    /**
     * Converts a local Ghana number (0XXXXXXXXX) to international format (233XXXXXXXXX).
     * Numbers already in international format or with '+' prefix are handled as well.
     */
    private String toInternational(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("233")) return digits;
        if (digits.startsWith("0"))   return "233" + digits.substring(1);
        return digits; // already bare international or unknown format
    }
}
