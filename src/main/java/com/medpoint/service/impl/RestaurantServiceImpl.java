package com.medpoint.service.impl;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.*;
import com.medpoint.enums.*;
import com.medpoint.exception.*;
import com.medpoint.repository.*;
import com.medpoint.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final MenuItemRepository menuRepo;
    private final RestaurantTableRepository tableRepo;
    private final RestaurantOrderRepository orderRepo;
    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    // ── Menu ──────────────────────────────────────────────────────────────────

    @Override
    public List<MenuItemResponse> getAllMenuItems() {
        return menuRepo.findByActiveTrueOrderByNameAsc().stream().map(this::toMenuResponse).toList();
    }

    @Override
    public List<MenuItemResponse> getMenuByType(String type) {
        MenuItemType t = MenuItemType.valueOf(type.toUpperCase());
        return menuRepo.findByTypeAndActiveTrueOrderByNameAsc(t).stream().map(this::toMenuResponse).toList();
    }

    @Override @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest req) {
        MenuItem item = MenuItem.builder()
                .name(req.getName()).category(req.getCategory())
                .type(req.getType()).price(req.getPrice()).build();
        return toMenuResponse(menuRepo.save(item));
    }

    @Override @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest req) {
        MenuItem item = findMenuItem(id);
        item.setName(req.getName()); item.setCategory(req.getCategory());
        item.setType(req.getType()); item.setPrice(req.getPrice());
        return toMenuResponse(menuRepo.save(item));
    }

    @Override @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = findMenuItem(id);
        item.setActive(false);
        menuRepo.save(item);
    }

    @Override @Transactional
    public MenuItemResponse updateMenuItemPrice(Long id, PriceUpdateRequest req) {
        MenuItem item = findMenuItem(id);
        item.setPrice(req.getPrice());
        return toMenuResponse(menuRepo.save(item));
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    @Override
    public List<RestaurantTableResponse> getAllTables() {
        return tableRepo.findAllByOrderByTableNumberAsc().stream().map(this::toTableResponse).toList();
    }

    @Override @Transactional
    public RestaurantTableResponse addTable(TableRequest req) {
        if (tableRepo.existsByTableNumber(req.getTableNumber())) {
            throw new BusinessException("Table " + req.getTableNumber() + " already exists.");
        }
        RestaurantTable table = tableRepo.save(
                RestaurantTable.builder().tableNumber(req.getTableNumber()).build());
        return toTableResponse(table);
    }

    @Override @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = findTable(id);
        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new BusinessException("Cannot delete an occupied table.");
        }
        tableRepo.delete(table);
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    /**
     * Opens a table (seats guests) and creates a new order.
     * Returns OrderResponse (not RestaurantTableResponse) because frontend openOrder() expects OrderResponse.
     */
    @Override @Transactional
    public OrderResponse openTable(Long tableId) {
        RestaurantTable table = findTable(tableId);
        // If already occupied, return the existing open order
        RestaurantOrder existing = orderRepo.findByTableAndStatus(table, OrderStatus.OPEN).orElse(null);
        if (existing != null) {
            return toOrderResponse(existing);
        }
        table.setStatus(TableStatus.OCCUPIED);
        tableRepo.save(table);
        RestaurantOrder order = RestaurantOrder.builder().table(table).build();
        return toOrderResponse(orderRepo.save(order));
    }

    @Override
    public List<OrderResponse> getAllOpenOrders() {
        return orderRepo.findByStatus(OrderStatus.OPEN).stream().map(this::toOrderResponse).toList();
    }

//    @Override
//    public OrderResponse getOrder(Long tableId) {
//        RestaurantTable table = findTable(tableId);
//        RestaurantOrder order = orderRepo.findByTableAndStatus(table, OrderStatus.OPEN)
//                .orElseThrow(() -> new BusinessException("No open order for table " + tableId));
//        return toOrderResponse(order);
//    }


    @Override
    public OrderResponse getOrder(Long tableId) {
        RestaurantTable table = findTable(tableId);
        return orderRepo.findByTableAndStatus(table, OrderStatus.OPEN)
                .map(this::toOrderResponse)
                .orElseGet(() -> openTable(tableId)); // auto-open instead of throwing
    }

    @Override @Transactional
    public OrderResponse addOrUpdateOrderItem(Long tableId, OrderItemRequest req) {
        RestaurantTable table = findTable(tableId);
        RestaurantOrder order = orderRepo.findByTableAndStatus(table, OrderStatus.OPEN)
                .orElseThrow(() -> new BusinessException("No open order for table " + tableId));
        MenuItem menuItem = findMenuItem(req.getMenuItemId());

        OrderItem existing = order.getItems().stream()
                .filter(i -> i.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst().orElse(null);
        if (existing != null) {
            existing.setQuantity(req.getQuantity());
        } else {
            order.getItems().add(OrderItem.builder()
                    .order(order).menuItem(menuItem)
                    .quantity(req.getQuantity()).unitPrice(menuItem.getPrice()).build());
        }
        return toOrderResponse(orderRepo.save(order));
    }

    @Override @Transactional
    public OrderResponse removeOrderItem(Long tableId, Long menuItemId) {
        RestaurantTable table = findTable(tableId);
        RestaurantOrder order = orderRepo.findByTableAndStatus(table, OrderStatus.OPEN)
                .orElseThrow(() -> new BusinessException("No open order for table " + tableId));
        order.getItems().removeIf(i -> i.getMenuItem().getId().equals(menuItemId));
        return toOrderResponse(orderRepo.save(order));
    }

    @Override @Transactional
    public OrderResponse clearOrder(Long tableId) {
        RestaurantTable table = findTable(tableId);
        RestaurantOrder order = orderRepo.findByTableAndStatus(table, OrderStatus.OPEN)
                .orElseThrow(() -> new BusinessException("No open order for table " + tableId));
        order.getItems().clear();
        return toOrderResponse(orderRepo.save(order));
    }

    /**
     * PUT /orders/{orderId}/items — Replace all items on an existing order.
     * Frontend calls: updateOrderItems(orderId, items)
     */
    @Override @Transactional
    public OrderResponse updateOrderItems(Long orderId, List<OrderItemRequest> items) {
        RestaurantOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException("Cannot update a closed order.");
        }
        order.getItems().clear();
        for (OrderItemRequest req : items) {
            MenuItem menuItem = findMenuItem(req.getMenuItemId());
            order.getItems().add(OrderItem.builder()
                    .order(order).menuItem(menuItem)
                    .quantity(req.getQuantity()).unitPrice(menuItem.getPrice()).build());
        }
        return toOrderResponse(orderRepo.save(order));
    }

    @Override @Transactional
    public TransactionResponse billTable(Long tableId, Long staffId) {
        RestaurantTable table = findTable(tableId);
        RestaurantOrder order = orderRepo.findByTableAndStatus(table, OrderStatus.OPEN)
                .orElseThrow(() -> new BusinessException("No open order for table " + tableId));
        if (order.getItems().isEmpty()) {
            throw new BusinessException("Cannot bill an empty order.");
        }
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", staffId));

        BigDecimal total = BigDecimal.ZERO;
        List<TransactionLineItem> lineItems = new ArrayList<>();
        for (OrderItem oi : order.getItems()) {
            BigDecimal subtotal = oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
            total = total.add(subtotal);
            lineItems.add(TransactionLineItem.builder()
                    .name(oi.getMenuItem().getName())
                    .category(oi.getMenuItem().getCategory())
                    .kind(LineItemKind.ITEM).quantity(oi.getQuantity())
                    .unitPrice(oi.getUnitPrice()).subtotal(subtotal).build());
        }

        order.setStatus(OrderStatus.BILLED);
        order.setBilledAt(Instant.now());
        orderRepo.save(order);

        table.setStatus(TableStatus.AVAILABLE);
        tableRepo.save(table);

        String ref  = "RST-" + (txRepo.count() + 1001);
        String desc = "Table " + table.getTableNumber() + " — " + order.getItems().size() + " items";
        Transaction tx = Transaction.builder()
                .reference(ref).module(TxModule.RESTAURANT).amount(total)
                .staff(staff).description(desc).build();
        lineItems.forEach(li -> li.setTransaction(tx));
        tx.setLineItems(lineItems);
        return toTxResponse(txRepo.save(tx));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RestaurantTable findTable(Long id) {
        return tableRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Table", id));
    }

    private MenuItem findMenuItem(Long id) {
        return menuRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    private MenuItemResponse toMenuResponse(MenuItem m) {
        return MenuItemResponse.builder()
                .id(m.getId()).name(m.getName()).category(m.getCategory())
                .type(m.getType()).price(m.getPrice()).active(m.isActive()).build();
    }

    private RestaurantTableResponse toTableResponse(RestaurantTable t) {
        RestaurantOrder openOrder = orderRepo.findByTableAndStatus(t, OrderStatus.OPEN).orElse(null);
        return RestaurantTableResponse.builder()
                .id(t.getId()).tableNumber(t.getTableNumber()).status(t.getStatus())
                .activeOrderId(openOrder != null ? openOrder.getId() : null).build();
    }

    private OrderResponse toOrderResponse(RestaurantOrder o) {
        BigDecimal total = o.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return OrderResponse.builder()
                .id(o.getId()).tableNumber(o.getTable().getTableNumber())
                .status(o.getStatus()).total(total)
                .openedAt(o.getOpenedAt()).billedAt(o.getBilledAt())
                .items(o.getItems().stream().map(i -> OrderResponse.OrderItemDto.builder()
                        .orderItemId(i.getId()).menuItemId(i.getMenuItem().getId())
                        .menuItemName(i.getMenuItem().getName()).category(i.getMenuItem().getCategory())
                        .type(i.getMenuItem().getType().name()).quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).build()).toList())
                .build();
    }

    private TransactionResponse toTxResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).reference(t.getReference()).module(t.getModule())
                .amount(t.getAmount()).staffName(t.getStaff().getName())
                .description(t.getDescription()).status(t.getStatus())
                .lineItems(t.getLineItems().stream().map(li -> TransactionResponse.LineItemDto.builder()
                        .id(li.getId()).name(li.getName()).category(li.getCategory())
                        .kind(li.getKind()).quantity(li.getQuantity())
                        .unitPrice(li.getUnitPrice()).subtotal(li.getSubtotal()).build()).toList())
                .createdAt(Instant.from(t.getCreatedAt())).build();
    }




    @Transactional
    @Override
    public void freeTable(Long tableId) {
        RestaurantTable table = findTable(tableId);
        table.setStatus(TableStatus.AVAILABLE);
        tableRepo.save(table);
    }
}
