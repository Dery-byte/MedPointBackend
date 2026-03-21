package com.medpoint.service;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RestaurantService {
    List<MenuItemResponse> getAllMenuItems();
    List<MenuItemResponse> getMenuByType(String type);
    MenuItemResponse createMenuItem(MenuItemRequest req);
    MenuItemResponse updateMenuItem(Long id, MenuItemRequest req);
    void deleteMenuItem(Long id);
    MenuItemResponse updateMenuItemPrice(Long id, PriceUpdateRequest req);

    List<RestaurantTableResponse> getAllTables();
    RestaurantTableResponse addTable(TableRequest req);
    void deleteTable(Long id);

    /** Opens a table — creates an order if not already open. Returns OrderResponse. */
    OrderResponse openTable(Long tableId);

    /** Gets all currently open orders */
    List<OrderResponse> getAllOpenOrders();

    OrderResponse getOrder(Long tableId);



    OrderResponse addOrUpdateOrderItem(Long tableId, OrderItemRequest req);
    OrderResponse removeOrderItem(Long tableId, Long menuItemId);
    OrderResponse clearOrder(Long tableId);

    /** Updates all items on an existing order (PUT /orders/{id}/items) */
    OrderResponse updateOrderItems(Long orderId, List<OrderItemRequest> items);

    TransactionResponse billTable(Long tableId, Long staffId);

    @Transactional
    void freeTable(Long tableId);
}
