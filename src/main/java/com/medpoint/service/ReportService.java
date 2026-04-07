package com.medpoint.service;

import com.medpoint.dto.request.ReportRequest;
import com.medpoint.dto.response.ReportResponse;

import java.time.LocalDate;

public interface ReportService {
    ReportResponse generate(ReportRequest req);
    ReportResponse storeOrderReport(LocalDate from, LocalDate to);
    ReportResponse hotelReport(LocalDate from, LocalDate to);
    ReportResponse inventoryReport();
}
