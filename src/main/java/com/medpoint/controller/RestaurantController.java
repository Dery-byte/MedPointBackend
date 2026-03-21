package com.medpoint.controller;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.User;
import com.medpoint.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ── Menu Items ─────────────────────────────────────────────────────────────
    // Frontend path: /restaurant/menu-items

    @GetMapping("/menu-items")
    public ResponseEntity<List<MenuItemResponse>> getMenu() {
        return ResponseEntity.ok(restaurantService.getAllMenuItems());
    }

    @PostMapping("/menu-items")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageRestaurant(authentication)")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createMenuItem(request));
    }

    @PutMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageRestaurant(authentication)")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id,
                                                            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(id, request));
    }

    @DeleteMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageRestaurant(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        restaurantService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Menu item deleted."));
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    @GetMapping("/tables")
    public ResponseEntity<List<RestaurantTableResponse>> getTables() {
        return ResponseEntity.ok(restaurantService.getAllTables());
    }

    @PostMapping("/tables")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageRestaurant(authentication)")
    public ResponseEntity<RestaurantTableResponse> addTable(@Valid @RequestBody TableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.addTable(request));
    }

    @DeleteMapping("/tables/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageRestaurant(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        restaurantService.deleteTable(id);
        return ResponseEntity.ok(ApiResponse.ok("Table removed."));
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    /**
     * GET /restaurant/orders — get all open orders (used by frontend getOrders)
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(restaurantService.getAllOpenOrders());
    }

    /**
     * POST /restaurant/orders — open a new order for a table.
     * Frontend: openOrder(tableId) → POST /restaurant/orders { tableId }
     */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> openOrder(@RequestBody Map<String, Long> body,
                                                    @AuthenticationPrincipal User currentUser) {
        Long tableId = body.get("tableId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantService.openTable(tableId));
    }

    /**
     * GET /restaurant/orders/table/{tableId}
     * Frontend: getOrderByTable(tableId)
     */
    @GetMapping("/orders/table/{tableId}")
    public ResponseEntity<OrderResponse> getOrderByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(restaurantService.getOrder(tableId));
    }

    /**
     * PUT /restaurant/orders/{orderId}/items
     * Frontend: updateOrderItems(orderId, items)
     */
    @PutMapping("/orders/{orderId}/items")
    public ResponseEntity<OrderResponse> updateOrderItems(@PathVariable Long orderId,
                                                           @RequestBody Map<String, List<OrderItemRequest>> body) {
        List<OrderItemRequest> items = body.get("items");
        return ResponseEntity.ok(restaurantService.updateOrderItems(orderId, items));
    }

    /**
     * POST /restaurant/bill — bill a table.
     * Frontend: billTable({ tableId })
     */
    @PostMapping("/bill")
    public ResponseEntity<TransactionResponse> billTable(@Valid @RequestBody BillTableRequest request,
                                                          @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(restaurantService.billTable(request.getTableId(), currentUser.getId()));
    }


    @PostMapping("/orders/table/{tableId}/free")
    public ResponseEntity<ApiResponse<Void>> freeTable(@PathVariable Long tableId) {
        restaurantService.freeTable(tableId);
        return ResponseEntity.ok(ApiResponse.ok("Table is now available."));
    }



}
