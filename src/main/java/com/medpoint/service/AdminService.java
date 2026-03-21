package com.medpoint.service;
import com.medpoint.dto.response.DashboardResponse;
import com.medpoint.dto.response.RevenueResponse;
import com.medpoint.dto.response.StockAlertResponse;
import com.medpoint.dto.request.TransactionFilterRequest;

public interface AdminService {
    DashboardResponse getDashboard();
    RevenueResponse getRevenue(TransactionFilterRequest filter);
    StockAlertResponse getStockAlerts();
}
