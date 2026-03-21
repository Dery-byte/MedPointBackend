package com.medpoint.service.impl;
import com.medpoint.dto.request.DispenseIssueRequest;
import com.medpoint.dto.request.ServiceIssueRequest;
import com.medpoint.dto.request.TransactionFilterRequest;
import com.medpoint.dto.response.ServiceReceiptResponse;
import com.medpoint.dto.response.TransactionResponse;
import com.medpoint.entity.Drug;
import com.medpoint.entity.Transaction;
import com.medpoint.entity.TransactionLineItem;
import com.medpoint.entity.User;
import com.medpoint.enums.LineItemKind;
import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import com.medpoint.exception.BusinessException;
import com.medpoint.exception.InsufficientStockException;
import com.medpoint.exception.ResourceNotFoundException;
import com.medpoint.repository.DrugRepository;
import com.medpoint.repository.ProductRepository;
import com.medpoint.repository.TransactionRepository;
import com.medpoint.repository.UserRepository;
import com.medpoint.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository txRepo;
    private final UserRepository userRepo;
    private final DrugRepository drugRepo;
    private final ProductRepository productRepo;

    @Override
    public List<TransactionResponse> getAll(TransactionFilterRequest filter) {
        TxModule module = filter.getModule();
        User staff = filter.getStaffId() != null
                ? userRepo.findById(filter.getStaffId()).orElse(null) : null;
        TransactionStatus status = filter.getStatus();

        Instant from = filter.getFromDate() != null
                ? filter.getFromDate().atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant to   = filter.getToDate() != null
                ? filter.getToDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;

        return txRepo.findFiltered(module, staff, status, from, to)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public TransactionResponse getById(Long id) {
        return toResponse(txRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id)));
    }

    @Override
    @Transactional
    public TransactionResponse cancel(Long id, Long cancelledByUserId) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        if (tx.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Transaction " + tx.getReference() + " is already cancelled.");
        }
        User cancelledBy = userRepo.findById(cancelledByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", cancelledByUserId));

        // Restore stock for Drugstore and Mart cancellations
        if (tx.getModule() == TxModule.DRUGSTORE) {
            tx.getLineItems().forEach(li -> {
                if (li.getKind().name().equals("ITEM")) {
                    drugRepo.findByActiveTrueOrderByNameAsc().stream()
                            .filter(d -> d.getName().equals(li.getName()))
                            .findFirst()
                            .ifPresent(d -> {
                                d.setStock(d.getStock() + li.getQuantity());
                                drugRepo.save(d);
                            });
                }
            });
        }
        if (tx.getModule() == TxModule.MART) {
            tx.getLineItems().forEach(li ->
                    productRepo.findByActiveTrueOrderByNameAsc().stream()
                            .filter(p -> p.getName().equals(li.getName()))
                            .findFirst()
                            .ifPresent(p -> {
                                p.setStock(p.getStock() + li.getQuantity());
                                productRepo.save(p);
                            }));
        }

        tx.setStatus(TransactionStatus.CANCELLED);
        tx.setCancelledBy(cancelledBy);
        tx.setCancelledAt(Instant.now());
        return toResponse(txRepo.save(tx));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).reference(t.getReference()).module(t.getModule())
                .amount(t.getAmount()).staffName(t.getStaff().getName())
                .description(t.getDescription()).status(t.getStatus())
                .lineItems(t.getLineItems().stream().map(li -> TransactionResponse.LineItemDto.builder()
                        .id(li.getId()).name(li.getName()).category(li.getCategory())
                        .kind(li.getKind()).quantity(li.getQuantity())
                        .unitPrice(li.getUnitPrice()).subtotal(li.getSubtotal()).build()).toList())
                .cancelledByName(t.getCancelledBy() != null ? t.getCancelledBy().getName() : null)
                .cancelledAt(t.getCancelledAt()).createdAt(t.getCreatedAt()).build();
    }























//
//
//    @Transactional
//    @Override
//    public ServiceReceiptResponse issue(ServiceIssueRequest req, Long staffId) {
//        User staff = userRepo.findById(staffId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", staffId));
//
//        // ── 1. Reduce drug stock for prescribed items ────────────────────────
//        List<ServiceIssueRequest.ItemLineDto> drugItems = req.items() == null ? List.of()
//                : req.items().stream()
//                .filter(i -> drugRepo.existsById(i.id()))
//                .toList();
//
//        if (!drugItems.isEmpty()) {
//            Map<Long, Drug> drugMap = drugRepo
//                    .findAllById(drugItems.stream().map(ServiceIssueRequest.ItemLineDto::id).toList())
//                    .stream()
//                    .collect(Collectors.toMap(Drug::getId, d -> d));
//
//            for (ServiceIssueRequest.ItemLineDto item : drugItems) {
//                Drug drug = drugMap.get(item.id());
//                if (drug == null) throw new ResourceNotFoundException("Drug", item.id());
//                if (drug.getStock() < item.qty())
//                    throw new InsufficientStockException(drug.getName(), drug.getStock(), item.qty());
//                drug.setStock(drug.getStock() - item.qty());
//            }
//            drugRepo.saveAll(drugMap.values());
//        }
//
//        // ── 2. Compute totals ────────────────────────────────────────────────
//        BigDecimal svcTotal = req.services().stream()
//                .map(ServiceIssueRequest.ServiceLineDto::price)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal itemTotal = req.items() == null ? BigDecimal.ZERO
//                : req.items().stream()
//                .map(i -> i.price().multiply(BigDecimal.valueOf(i.qty())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal grandTotal = svcTotal.add(itemTotal);
//
//        // ── 3. Build description ─────────────────────────────────────────────
//        String desc = "Services: " + req.services().stream()
//                .map(ServiceIssueRequest.ServiceLineDto::name)
//                .collect(Collectors.joining(", "));
//
//        // ── 4. Persist transaction + line items ──────────────────────────────
//        String reference = generateReference();
//
//        Transaction tx = Transaction.builder()
//                .reference(reference)
//                .module(TxModule.DRUGSTORE)
//                .amount(grandTotal)
//                .staff(staff)
//                .description(desc)
//                .build();
//
//        List<TransactionLineItem> lineItems = new ArrayList<>();
//
//        for (ServiceIssueRequest.ServiceLineDto svc : req.services()) {
//            lineItems.add(TransactionLineItem.builder()
//                    .transaction(tx)
//                    .name(svc.name())
//                    .category(svc.cat())
//                    .kind(LineItemKind.SERVICE)
//                    .quantity(1)
//                    .unitPrice(svc.price())
//                    .subtotal(svc.price())
//                    .build());
//        }
//
//        if (req.items() != null) {
//            for (ServiceIssueRequest.ItemLineDto item : req.items()) {
//                BigDecimal subtotal = item.price().multiply(BigDecimal.valueOf(item.qty()));
//                lineItems.add(TransactionLineItem.builder()
//                        .transaction(tx)
//                        .name(item.name())
//                        .category(item.cat())
//                        .kind(LineItemKind.ITEM)
//                        .quantity(item.qty())
//                        .unitPrice(item.price())
//                        .subtotal(subtotal)
//                        .build());
//            }
//        }
//
//        tx.getLineItems().addAll(lineItems);
//        Transaction saved = txRepo.save(tx);
//
//        // ── 5. Map to receipt response ───────────────────────────────────────
//        List<ServiceReceiptResponse.LineDto> lineDtos = saved.getLineItems().stream()
//                .map(li -> new ServiceReceiptResponse.LineDto(
//                        li.getName(),
//                        li.getCategory(),
//                        li.getKind().name(),
//                        li.getQuantity(),
//                        li.getUnitPrice(),
//                        li.getSubtotal()
//                )).toList();
//
//        return new ServiceReceiptResponse(
//                saved.getId(),
//                saved.getReference(),
//                svcTotal,
//                itemTotal,
//                grandTotal,
//                saved.getCreatedAt(),
//                lineDtos
//        );
//    }
//
//
//    // ── reference generator ──────────────────────────────────────────────────
//    private String generateReference() {
//        long count = txRepo.count();
//        return "TX-%04d".formatted(1000 + count + 1);
//    }
//




    @Transactional
    @Override
    public ServiceReceiptResponse issue(ServiceIssueRequest req, Long staffId) {
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", staffId));

        // Treat null and empty list the same way
        List<ServiceIssueRequest.ItemLineDto> allItems =
                (req.items() == null || req.items().isEmpty()) ? List.of() : req.items();

        // ── 1. Reduce stock only for items that are drugs ────────────────────
        List<ServiceIssueRequest.ItemLineDto> drugItems = allItems.stream()
                .filter(i -> drugRepo.existsById(i.id()))
                .toList();

        if (!drugItems.isEmpty()) {
            Map<Long, Drug> drugMap = drugRepo
                    .findAllById(drugItems.stream()
                            .map(ServiceIssueRequest.ItemLineDto::id)
                            .toList())
                    .stream()
                    .collect(Collectors.toMap(Drug::getId, d -> d));

            for (ServiceIssueRequest.ItemLineDto item : drugItems) {
                Drug drug = drugMap.get(item.id());
                if (drug == null)
                    throw new ResourceNotFoundException("Drug", item.id());
                if (drug.getStock() < item.qty())
                    throw new InsufficientStockException(drug.getName(), drug.getStock(), item.qty());
                drug.setStock(drug.getStock() - item.qty());
            }
            drugRepo.saveAll(drugMap.values());
        }

        // ── 2. Compute totals ────────────────────────────────────────────────
        BigDecimal svcTotal = req.services().stream()
                .map(ServiceIssueRequest.ServiceLineDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemTotal = allItems.stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.qty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandTotal = svcTotal.add(itemTotal);

        // ── 3. Build description ─────────────────────────────────────────────
//        String desc = "Services: " + req.services().stream()
//                .map(ServiceIssueRequest.ServiceLineDto::name)
//                .collect(Collectors.joining(", "));

        // ── 3. Build description ─────────────────────────────────────────────
        String desc = "Service (s) (" + req.services().size() + " provided)";
        // ── 4. Persist transaction + line items ──────────────────────────────
        Transaction tx = Transaction.builder()
                .reference(generateReference())
                .module(TxModule.DRUGSTORE)
                .amount(grandTotal)
                .staff(staff)
                .description(desc)
                .build();

        List<TransactionLineItem> lineItems = new ArrayList<>();

        // One line item per selected service
        for (ServiceIssueRequest.ServiceLineDto svc : req.services()) {
            lineItems.add(TransactionLineItem.builder()
                    .transaction(tx)
                    .name(svc.name())
                    .category(svc.cat())
                    .kind(LineItemKind.SERVICE)
                    .quantity(1)
                    .unitPrice(svc.price())
                    .subtotal(svc.price())
                    .build());
        }

        // One line item per cart item (drug or non-drug)
        for (ServiceIssueRequest.ItemLineDto item : allItems) {
            BigDecimal subtotal = item.price().multiply(BigDecimal.valueOf(item.qty()));
            lineItems.add(TransactionLineItem.builder()
                    .transaction(tx)
                    .name(item.name())
                    .category(item.cat())
                    .kind(LineItemKind.ITEM)
                    .quantity(item.qty())
                    .unitPrice(item.price())
                    .subtotal(subtotal)
                    .build());
        }

        tx.getLineItems().addAll(lineItems);
        Transaction saved = txRepo.save(tx);

        // ── 5. Map to receipt response ───────────────────────────────────────
        List<ServiceReceiptResponse.LineDto> lineDtos = saved.getLineItems().stream()
                .map(li -> new ServiceReceiptResponse.LineDto(
                        li.getName(),
                        li.getCategory(),
                        li.getKind().name(),
                        li.getQuantity(),
                        li.getUnitPrice(),
                        li.getSubtotal()
                )).toList();

        return new ServiceReceiptResponse(
                saved.getId(),
                saved.getReference(),
                svcTotal,
                itemTotal,
                grandTotal,
                saved.getCreatedAt(),
                lineDtos
        );
    }

    private String generateReference() {
        long count = txRepo.count();
        return "TX-%04d".formatted(1000 + count + 1);
    }







    @Transactional
    @Override
    public ServiceReceiptResponse dispense(DispenseIssueRequest req, Long staffId) {
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", staffId));

        // ── 1. Validate and reduce stock for all items (all are drugs) ───────
        Map<Long, Drug> drugMap = drugRepo
                .findAllById(req.items().stream()
                        .map(DispenseIssueRequest.ItemLineDto::id)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        for (DispenseIssueRequest.ItemLineDto item : req.items()) {
            Drug drug = drugMap.get(item.id());
            if (drug == null)
                throw new ResourceNotFoundException("Drug", item.id());
            if (drug.getStock() < item.qty())
                throw new InsufficientStockException(drug.getName(), drug.getStock(), item.qty());
            drug.setStock(drug.getStock() - item.qty());
        }
        drugRepo.saveAll(drugMap.values());

        // ── 2. Compute total ─────────────────────────────────────────────────
        BigDecimal grandTotal = req.items().stream()
                .map(i -> i.price().multiply(BigDecimal.valueOf(i.qty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── 3. Build description ─────────────────────────────────────────────
        String desc = "Drug Dispense (" + req.items().size() + " items)";

        // ── 4. Persist transaction + line items ──────────────────────────────
        Transaction tx = Transaction.builder()
                .reference(generateReference())
                .module(TxModule.DRUGSTORE)
                .amount(grandTotal)
                .staff(staff)
                .description(desc)
                .build();

        List<TransactionLineItem> lineItems = req.items().stream()
                .map(item -> {
                    BigDecimal subtotal = item.price().multiply(BigDecimal.valueOf(item.qty()));
                    return TransactionLineItem.builder()
                            .transaction(tx)
                            .name(item.name())
                            .category(item.cat())
                            .kind(LineItemKind.DRUG)
                            .quantity(item.qty())
                            .unitPrice(item.price())
                            .subtotal(subtotal)
                            .build();
                }).toList();

        tx.getLineItems().addAll(lineItems);
        Transaction saved = txRepo.save(tx);

        // ── 5. Map to receipt response ───────────────────────────────────────
        List<ServiceReceiptResponse.LineDto> lineDtos = saved.getLineItems().stream()
                .map(li -> new ServiceReceiptResponse.LineDto(
                        li.getName(),
                        li.getCategory(),
                        li.getKind().name(),
                        li.getQuantity(),
                        li.getUnitPrice(),
                        li.getSubtotal()
                )).toList();

        return new ServiceReceiptResponse(
                saved.getId(),
                saved.getReference(),
                BigDecimal.ZERO,   // no svcTotal — dispense has no services
                grandTotal,
                grandTotal,
                saved.getCreatedAt(),
                lineDtos
        );
    }
}
