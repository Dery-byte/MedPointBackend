package com.medpoint.service;

import com.medpoint.dto.request.StoreOrderRequest;
import com.medpoint.dto.response.StoreOrderResponse;

import java.util.List;

public interface StoreOrderService {
    StoreOrderResponse createOrder(StoreOrderRequest req, Long customerId);
    List<StoreOrderResponse> getAllOrders();
    StoreOrderResponse updateOrderStatus(Long orderId, String status, String changedBy);
    List<StoreOrderResponse> getOrdersByCustomer(Long customerId);
}
