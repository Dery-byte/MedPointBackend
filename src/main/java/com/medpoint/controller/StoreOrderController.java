package com.medpoint.controller;

import com.medpoint.dto.request.StoreOrderRequest;
import com.medpoint.dto.request.UpdateOrderStatusRequest;
import com.medpoint.dto.response.StoreOrderResponse;
import com.medpoint.entity.User;
import com.medpoint.security.CustomerDetails;
import com.medpoint.service.StoreOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreOrderController {

    private final StoreOrderService storeOrderService;

    /**
     * POST /store/orders — public endpoint (guest checkout or authenticated customer).
     * If the request carries a customer JWT, the order is linked to that customer.
     */
    @PostMapping("/orders")
    public ResponseEntity<StoreOrderResponse> createOrder(
            @Valid @RequestBody StoreOrderRequest req,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomerDetails currentCustomer) {
        Long customerId = currentCustomer != null ? currentCustomer.getId() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storeOrderService.createOrder(req, customerId));
    }

    /**
     * GET /store/orders — admin view, requires SUPERADMIN or MANAGER role.
     */
    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'MANAGER')")
    public ResponseEntity<List<StoreOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(storeOrderService.getAllOrders());
    }

    /**
     * PATCH /store/orders/{id}/status — update order status, staff only.
     * Records the name of the staff member who made the change.
     */
    @PatchMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'MANAGER')")
    public ResponseEntity<StoreOrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest req,
            @AuthenticationPrincipal User staffUser) {
        String changedBy = staffUser != null ? staffUser.getName() : "Unknown";
        return ResponseEntity.ok(storeOrderService.updateOrderStatus(id, req.getStatus(), changedBy));
    }

    /**
     * GET /store/orders/my — returns orders for the authenticated customer.
     */
    @GetMapping("/orders/my")
    public ResponseEntity<List<StoreOrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomerDetails customer) {
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(storeOrderService.getOrdersByCustomer(customer.getId()));
    }
}
