package com.medpoint.controller;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.User;
import com.medpoint.service.AdminService;
import com.medpoint.service.TransactionService;
import com.medpoint.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final TransactionService transactionService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /** GET /api/admin/dashboard */
//    @GetMapping("/dashboard")
//    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
//    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
//        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboard()));
//    }


    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard fetched successfully.", adminService.getDashboard()));
    }

    // ── Revenue ───────────────────────────────────────────────────────────────

    /** GET /api/admin/revenue */
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
    public ResponseEntity<RevenueResponse> getRevenue(TransactionFilterRequest filter) {
        return ResponseEntity.ok(adminService.getRevenue(filter));
    }

    // ── Stock alerts ──────────────────────────────────────────────────────────

    /** GET /api/admin/stock-alerts */
    @GetMapping("/stock-alerts")
    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
    public ResponseEntity<StockAlertResponse> getStockAlerts() {
        return ResponseEntity.ok(adminService.getStockAlerts());
    }

    // ── Staff / User management ───────────────────────────────────────────────

    /** GET /api/admin/users */
    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** POST /api/admin/users */
    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    /** PUT /api/admin/users/{id} */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                    @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    /** PATCH /api/admin/users/{id}/toggle */
    @PatchMapping("/users/{id}/toggle")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleActive(id));
    }

    // ── Transactions ──────────────────────────────────────────────────────────

    /** GET /api/admin/transactions */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
    public ResponseEntity<List<TransactionResponse>> getTransactions(TransactionFilterRequest filter) {
        return ResponseEntity.ok(transactionService.getAll(filter));
    }

    /** PATCH /api/admin/transactions/{id}/cancel */
    @PatchMapping("/transactions/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPERADMIN','MANAGER')")
    public ResponseEntity<TransactionResponse> cancelTransaction(@PathVariable Long id,
                                                                  @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(transactionService.cancel(id, currentUser.getId()));
    }
}
