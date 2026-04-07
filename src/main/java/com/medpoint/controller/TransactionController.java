package com.medpoint.controller;
import com.medpoint.dto.paystackdto.PaymentResponse;
import com.medpoint.dto.request.DispenseIssueRequest;
import com.medpoint.dto.request.MartCheckoutRequest;
import com.medpoint.dto.request.ServiceIssueRequest;
import com.medpoint.dto.request.TransactionFilterRequest;
import com.medpoint.dto.response.ServiceReceiptResponse;
import com.medpoint.dto.response.TransactionResponse;
import com.medpoint.entity.Drug;
import com.medpoint.entity.Transaction;
import com.medpoint.entity.TransactionLineItem;
import com.medpoint.entity.User;
import com.medpoint.enums.LineItemKind;
import com.medpoint.enums.TxModule;
import com.medpoint.exception.InsufficientStockException;
import com.medpoint.exception.ResourceNotFoundException;
import com.medpoint.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles cross-module transaction queries from the frontend transactionService.ts:
 *   GET  /transactions           → getAll(filters)
 *   GET  /transactions/{id}      → getById(id)
 *   PATCH /transactions/{id}/cancel → cancel(id)
 */
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(TransactionFilterRequest filter) {
        return ResponseEntity.ok(transactionService.getAll(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TransactionResponse> cancel(@PathVariable Long id,
                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(transactionService.cancel(id, currentUser.getId()));
    }


    /** POST /api/mart/checkout — process a cart sale */
    @PostMapping("/issue")
    public ResponseEntity<ServiceReceiptResponse> issueReciept(@Valid @RequestBody ServiceIssueRequest request, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.issue(request, currentUser.getId()));
    }

    @PostMapping("/dispense")
    public ResponseEntity<ServiceReceiptResponse> dispense(
            @Valid @RequestBody DispenseIssueRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(transactionService.dispense(request, currentUser.getId()));
    }





    /** GET /api/transactions/{reference} */
    @GetMapping("/{reference}")
    public ResponseEntity<PaymentResponse> getTransaction(@PathVariable String reference) {
        return ResponseEntity.ok(transactionService.getByReference(reference));
    }

    /** GET /api/transactions?page=0&size=20&sort=createdAt,desc */
    @GetMapping("/getAllOnlinetransaction")
    public ResponseEntity<Page<Transaction>> listTransactions(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(transactionService.listAll(pageable));
    }

}
