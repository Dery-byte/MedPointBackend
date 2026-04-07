package com.medpoint.service.impl;

import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.Customer;
import com.medpoint.exception.BusinessException;
import com.medpoint.repository.CustomerRepository;
import com.medpoint.repository.StoreOrderRepository;
import com.medpoint.security.CustomerDetails;
import com.medpoint.security.JwtService;
import com.medpoint.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepo;
    private final StoreOrderRepository orderRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CustomerAuthResponse register(CustomerRegisterRequest req) {
        if (customerRepo.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already registered: " + req.getEmail());
        }
        Customer customer = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        Customer saved = customerRepo.save(customer);
        String token = jwtService.generateToken(new CustomerDetails(saved), "customer");
        return CustomerAuthResponse.builder()
                .token(token).id(saved.getId())
                .name(saved.getName()).email(saved.getEmail()).phone(saved.getPhone())
                .build();
    }

    @Override
    public CustomerAuthResponse login(CustomerLoginRequest req) {
        Customer customer = customerRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), customer.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }
        String token = jwtService.generateToken(new CustomerDetails(customer), "customer");
        return CustomerAuthResponse.builder()
                .token(token).id(customer.getId())
                .name(customer.getName()).email(customer.getEmail()).phone(customer.getPhone())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(Long customerId, UpdateCustomerProfileRequest req) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
        customer.setName(req.getName());
        customer.setPhone(req.getPhone());
        customerRepo.save(customer);
    }

    @Override
    @Transactional
    public void changePassword(Long customerId, ChangePasswordRequest req) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
        if (!passwordEncoder.matches(req.getCurrentPassword(), customer.getPassword())) {
            throw new BusinessException("Current password is incorrect");
        }
        customer.setPassword(passwordEncoder.encode(req.getNewPassword()));
        customerRepo.save(customer);
    }

    @Override
    public List<StoreOrderResponse> getMyOrders(Long customerId) {
        return orderRepo.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse).toList();
    }

    private StoreOrderResponse toResponse(com.medpoint.entity.StoreOrder order) {
        return StoreOrderResponse.builder()
                .id(order.getId())
                .reference(order.getReference())
                .status(order.getStatus())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(i -> StoreOrderResponse.Item.builder()
                        .productId(i.getProductId()).name(i.getName())
                        .quantity(i.getQuantity()).unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal()).build()).toList())
                .build();
    }
}
