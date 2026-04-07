package com.medpoint.controller;

import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.security.CustomerDetails;
import com.medpoint.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /** POST /customers/register — public */
    @PostMapping("/register")
    public ResponseEntity<CustomerAuthResponse> register(@Valid @RequestBody CustomerRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(req));
    }

    /** POST /customers/login — public */
    @PostMapping("/login")
    public ResponseEntity<CustomerAuthResponse> login(@Valid @RequestBody CustomerLoginRequest req) {
        return ResponseEntity.ok(customerService.login(req));
    }

    /** PUT /customers/me — update profile (requires customer JWT) */
    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal CustomerDetails currentCustomer,
                                               @Valid @RequestBody UpdateCustomerProfileRequest req) {
        customerService.updateProfile(currentCustomer.getId(), req);
        return ResponseEntity.noContent().build();
    }

    /** PUT /customers/me/password — change password (requires customer JWT) */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal CustomerDetails currentCustomer,
                                                @Valid @RequestBody ChangePasswordRequest req) {
        customerService.changePassword(currentCustomer.getId(), req);
        return ResponseEntity.noContent().build();
    }

    /** GET /customers/me/orders — get customer order history (requires customer JWT) */
    @GetMapping("/me/orders")
    public ResponseEntity<List<StoreOrderResponse>> getMyOrders(@AuthenticationPrincipal CustomerDetails currentCustomer) {
        return ResponseEntity.ok(customerService.getMyOrders(currentCustomer.getId()));
    }
}
