package com.medpoint.service;

import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;

import java.util.List;

public interface CustomerService {
    CustomerAuthResponse register(CustomerRegisterRequest req);
    CustomerAuthResponse login(CustomerLoginRequest req);
    void updateProfile(Long customerId, UpdateCustomerProfileRequest req);
    void changePassword(Long customerId, ChangePasswordRequest req);
    List<StoreOrderResponse> getMyOrders(Long customerId);
}
