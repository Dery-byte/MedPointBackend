package com.medpoint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaystackConfig {

    @Value("${paystack.secret-key}")
    private String secretKey;

    @Value("${paystack.public-key}")
    private String publicKey;

    @Value("${paystack.base-url}")
    private String baseUrl;

    @Value("${paystack.callback-url}")
    private String callbackUrl;

    /**
     * Pre-configured RestClient for all Paystack API calls.
     * RestClient ships with spring-boot-starter-web — no extra dependency needed.
     */
    @Bean
    public RestClient paystackRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String getSecretKey()  { return secretKey; }
    public String getPublicKey()  { return publicKey; }
    public String getBaseUrl()    { return baseUrl; }
    public String getCallbackUrl(){ return callbackUrl; }
}
