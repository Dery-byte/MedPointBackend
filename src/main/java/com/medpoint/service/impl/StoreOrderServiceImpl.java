package com.medpoint.service.impl;

import com.medpoint.dto.request.StoreOrderRequest;
import com.medpoint.dto.response.StoreOrderResponse;
import com.medpoint.entity.Customer;
import com.medpoint.entity.StoreOrder;
import com.medpoint.entity.StoreOrderItem;
import com.medpoint.entity.StoreOrderStatusHistory;
import com.medpoint.repository.CustomerRepository;
import com.medpoint.repository.StoreOrderRepository;
import com.medpoint.repository.StoreOrderStatusHistoryRepository;
import com.medpoint.service.SmsService;
import com.medpoint.service.StoreOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreOrderServiceImpl implements StoreOrderService {

    /**
     * One-directional status flow:
     *   pending → processing → delivered (terminal)
     *   Any non-delivered status can be cancelled.
     *   Cancelled can be restored back to pending.
     */
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "pending",    Set.of("processing", "cancelled"),
            "processing", Set.of("delivered",  "cancelled"),
            "delivered",  Set.of(),
            "cancelled",  Set.of("pending")
    );

    private final StoreOrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final StoreOrderStatusHistoryRepository historyRepo;
    private final SmsService smsService;

    @Override
    @Transactional
    public StoreOrderResponse createOrder(StoreOrderRequest req, Long customerId) {
        Customer customer = customerId != null
                ? customerRepo.findById(customerId).orElse(null)
                : null;

        String reference = "SO-" + System.currentTimeMillis();

        StoreOrder order = StoreOrder.builder()
                .reference(reference)
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .deliveryAddress(req.getDeliveryAddress())
                .total(req.getTotal())
                .paymentMethod(req.getPaymentMethod())
                .paymentReference(req.getPaymentReference())
                .customer(customer)
                .items(new ArrayList<>())
                .build();

        for (StoreOrderRequest.OrderItem item : req.getItems()) {
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            StoreOrderItem lineItem = StoreOrderItem.builder()
                    .order(order)
                    .productId(item.getProductId())
                    .name(item.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(subtotal)
                    .imageUrl(item.getImageUrl())
                    .build();
            order.getItems().add(lineItem);
        }

        StoreOrder saved = orderRepo.save(order);
        return toResponse(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreOrderResponse> getAllOrders() {
        List<StoreOrder> orders = orderRepo.findAll();
        if (orders.isEmpty()) return List.of();

        List<Long> orderIds = orders.stream().map(StoreOrder::getId).toList();

        // Group history by orderId using an explicit loop — avoids stream/lazy-proxy issues
        Map<Long, List<StoreOrderStatusHistory>> historyMap = new HashMap<>();
        try {
            List<StoreOrderStatusHistory> allHistory = historyRepo.findByOrderIds(orderIds);
            for (StoreOrderStatusHistory h : allHistory) {
                Long oid = h.getOrder().getId();
                historyMap.computeIfAbsent(oid, k -> new ArrayList<>()).add(h);
            }
        } catch (Exception ex) {
            // History table may not exist yet on first boot before DDL completes
            log.warn("Could not load status history (table may not exist yet): {}", ex.getMessage());
        }

        List<StoreOrderResponse> result = new ArrayList<>();
        for (StoreOrder o : orders) {
            List<StoreOrderStatusHistory> hist = historyMap.getOrDefault(o.getId(), List.of());
            result.add(toResponse(o, hist));
        }
        return result;
    }

    @Override
    @Transactional
    public StoreOrderResponse updateOrderStatus(Long orderId, String status, String changedBy) {
        String newStatus = status.toLowerCase();

        StoreOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found: " + orderId));

        String currentStatus = (order.getStatus() != null ? order.getStatus() : "pending").toLowerCase();

        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            String msg = allowed.isEmpty()
                    ? "Order is in a terminal state (" + currentStatus + ") and cannot be updated."
                    : "Cannot transition from '" + currentStatus + "' to '" + newStatus
                      + "'. Allowed: " + allowed;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }

        // Save history entry
        StoreOrderStatusHistory entry = StoreOrderStatusHistory.builder()
                .order(order)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build();
        historyRepo.save(entry);

        // Update order
        order.setStatus(newStatus);
        order.setStatusChangedBy(changedBy);
        order.setStatusChangedAt(entry.getChangedAt());
        StoreOrder saved = orderRepo.save(order);

        // SMS notification when order starts processing
        if ("processing".equals(newStatus) && order.getPhone() != null) {
            String msg = String.format(
                "Hi %s, your MedPoint order %s is now being processed. We'll update you once it's on the way. Thank you!",
                order.getCustomerName(), order.getReference()
            );
            smsService.send(order.getPhone(), msg);
        }

        List<StoreOrderStatusHistory> history = historyRepo.findByOrderId(orderId);
        return toResponse(saved, history);
    }

    @Override
    public List<StoreOrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepo.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(o -> toResponse(o, null))
                .toList();
    }

    // ── Mapping ────────────────────────────────────────────────────────────────

    private StoreOrderResponse toResponse(StoreOrder order, List<StoreOrderStatusHistory> history) {
        List<StoreOrderResponse.StatusHistoryEntry> historyDtos = null;
        if (history != null) {
            historyDtos = new ArrayList<>();
            for (StoreOrderStatusHistory h : history) {
                historyDtos.add(StoreOrderResponse.StatusHistoryEntry.builder()
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build());
            }
        }

        return StoreOrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .statusChangedBy(order.getStatusChangedBy())
                .statusChangedAt(order.getStatusChangedAt())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(i -> StoreOrderResponse.Item.builder()
                        .productId(i.getProductId())
                        .name(i.getName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .imageUrl(i.getImageUrl())
                        .build()).toList())
                .statusHistory(historyDtos)
                .build();
    }
}
