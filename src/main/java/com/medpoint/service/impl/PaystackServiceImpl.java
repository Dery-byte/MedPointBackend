package com.medpoint.service.impl;
import com.medpoint.config.PaystackConfig;
import com.medpoint.dto.paystackdto.InitializePaymentRequest;
import com.medpoint.dto.paystackdto.PaystackApiResponse;
import com.medpoint.dto.response.StorePaymentResponse;
import com.medpoint.entity.Transaction;
import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import com.medpoint.repository.TransactionRepository;
import com.medpoint.service.PaystackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaystackServiceImpl implements PaystackService {

    private final RestClient paystackRestClient;
  private final TransactionRepository transactionRepository;
  private final PaystackConfig config;

    // ── 1. Initialize ────────────────────────────────────────────────────

    @Override
    @Transactional
    public StorePaymentResponse initializePayment(InitializePaymentRequest request) {
        String reference = generateReference();

        Map<String, Object> body = new HashMap<>();
        body.put("email",        request.getEmail());
        body.put("amount",       toSubunits(request.getAmount()));
        body.put("reference",    reference);
        body.put("callback_url",
                request.getCallbackUrl() != null ? request.getCallbackUrl() : config.getCallbackUrl());

        if (request.getCurrency()    != null) body.put("currency",    request.getCurrency());
        if (request.getDescription() != null) body.put("description", request.getDescription());

        log.debug("Initializing Paystack transaction | ref={} amount={}", reference, request.getAmount());

        PaystackApiResponse apiResp = paystackRestClient.post()
                .uri("/transaction/initialize")
                .body(body)
                .retrieve()
                .body(PaystackApiResponse.class);

        if (apiResp == null || !apiResp.isStatus()) {
            String msg = apiResp != null ? apiResp.getMessage() : "No response from Paystack";
            log.error("Paystack init failed: {}", msg);
            return StorePaymentResponse.error("Payment initialization failed: " + msg);
        }

        PaystackApiResponse.Data data = apiResp.getData();
        Transaction tx = Transaction.builder()
                .reference(reference)
                .email(request.getEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "GHS")
                .onlineStatus(Transaction.OnlineTransactionStatus.PENDING)
                .status(TransactionStatus.ACTIVE)
                .authorizationUrl(data.getAuthorizationUrl())
                .accessCode(data.getAccessCode())
                .description(request.getDescription() != null ? request.getDescription() : "Online Payment")
                .module(TxModule.STORE)        // ← add an ONLINE value to your TxModule enum
                .staff(request.getStaff())      // ← or a system/default staff user
                .build();

        transactionRepository.save(tx);
        log.info("Transaction saved | ref={} status=PENDING", reference);

        return StorePaymentResponse.from(tx, "Payment initialized. Redirect customer to authorizationUrl.");
    }

    // ── 2. Verify ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public StorePaymentResponse verifyTransaction(String reference) {
        log.debug("Verifying transaction | ref={}", reference);
        Transaction tx = transactionRepository.findByReference(reference)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + reference));

        if (tx.getOnlineStatus() == Transaction.OnlineTransactionStatus.SUCCESS) {
            return StorePaymentResponse.from(tx, "Transaction already verified as successful.");
        }

        PaystackApiResponse apiResp = paystackRestClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .body(PaystackApiResponse.class);

        if (apiResp == null || !apiResp.isStatus()) {
            String msg = apiResp != null ? apiResp.getMessage() : "No response from Paystack";
            return StorePaymentResponse.error("Verification failed: " + msg);
        }

        PaystackApiResponse.Data data = apiResp.getData();
        updateTransactionFromData(tx, data);
        transactionRepository.save(tx);

        log.info("Transaction verified | ref={} paystackStatus={}", reference, data.getStatus());
        return StorePaymentResponse.from(tx, "Transaction verified: " + data.getStatus());
    }
    // ── 3. Webhook ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void handleWebhookEvent(String event, PaystackApiResponse.Data data) {
        if (!"charge.success".equals(event)) {
            log.debug("Ignoring non-payment event: {}", event);
            return;
        }

        String reference = data.getReference();
        log.info("charge.success webhook received | ref={}", reference);

        transactionRepository.findByReference(reference).ifPresentOrElse(tx -> {
            if (tx.getOnlineStatus() == Transaction.OnlineTransactionStatus.SUCCESS) {
                log.warn("Duplicate webhook for ref={} — already SUCCESS, skipping", reference);
                return;
            }
            updateTransactionFromData(tx, data);
            transactionRepository.save(tx);
            log.info("Transaction updated via webhook | ref={} -> {}", reference, tx.getStatus());
            onPaymentSuccess(tx);
        }, () -> log.warn("Webhook received for unknown reference: {}", reference));
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void updateTransactionFromData(Transaction tx, PaystackApiResponse.Data data) {
        switch (data.getStatus()) {
            case "success"   -> tx.setOnlineStatus(Transaction.OnlineTransactionStatus.SUCCESS);
            case "failed"    -> tx.setOnlineStatus(Transaction.OnlineTransactionStatus.FAILED);
            case "abandoned" -> tx.setOnlineStatus(Transaction.OnlineTransactionStatus.ABANDONED);
            default          -> tx.setOnlineStatus(Transaction.OnlineTransactionStatus.PENDING);
        }
        if (data.getId() != null) tx.setPaystackId(data.getId());
        if (data.getAuthorization() != null
                && Boolean.TRUE.equals(data.getAuthorization().getReusable())) {
            tx.setAuthorizationCode(data.getAuthorization().getAuthorizationCode());
            log.info("Reusable authorization saved | ref={}", tx.getReference());
        }
    }

    /**
     * Hook called after every confirmed successful payment.
     * Inject and call your own services here (email, orders, wallets, etc.)
     */
    protected void onPaymentSuccess(Transaction tx) {
        log.info("✅ Payment successful — deliver value for ref={} amount={} {}",
                tx.getReference(), tx.getAmount(), tx.getCurrency());
    }


    private long toSubunits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }



    private String generateReference() {
        long count = transactionRepository.count() + 1;
        return String.format("STR-%04d", count);
    }
}
