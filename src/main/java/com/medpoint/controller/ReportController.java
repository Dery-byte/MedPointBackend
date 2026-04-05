package com.medpoint.controller;

import com.medpoint.dto.request.ReportRequest;
import com.medpoint.dto.response.ReportResponse;
import com.medpoint.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/reports")
@PreAuthorize("hasAnyRole('SUPERADMIN', 'MANAGER')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /admin/reports
     * Query params: module, staffId, fromDate (yyyy-MM-dd), toDate, groupBy (daily|monthly|staff|category|product)
     */
    @GetMapping
    public ResponseEntity<ReportResponse> generate(ReportRequest req) {
        return ResponseEntity.ok(reportService.generate(req));
    }

    /**
     * GET /admin/reports/store-orders?fromDate=&toDate=
     */
    @GetMapping("/store-orders")
    public ResponseEntity<ReportResponse> storeOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.storeOrderReport(fromDate, toDate));
    }

    /**
     * GET /admin/reports/hotel?fromDate=&toDate=
     */
    @GetMapping("/hotel")
    public ResponseEntity<ReportResponse> hotel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.hotelReport(fromDate, toDate));
    }

    /**
     * GET /admin/reports/inventory
     */
    @GetMapping("/inventory")
    public ResponseEntity<ReportResponse> inventory() {
        return ResponseEntity.ok(reportService.inventoryReport());
    }
}
