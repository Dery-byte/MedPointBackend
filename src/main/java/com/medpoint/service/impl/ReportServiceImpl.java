//package com.medpoint.service.impl;
//
//import com.medpoint.dto.request.ReportRequest;
//import com.medpoint.dto.response.ReportResponse;
//import com.medpoint.dto.response.ReportResponse.GroupRow;
//import com.medpoint.dto.response.ReportResponse.ItemRow;
//import com.medpoint.entity.Booking;
//import com.medpoint.entity.Drug;
//import com.medpoint.entity.StoreOrder;
//import com.medpoint.entity.Transaction;
//import com.medpoint.entity.TransactionLineItem;
//import com.medpoint.enums.TransactionStatus;
//import com.medpoint.repository.BookingRepository;
//import com.medpoint.repository.DrugRepository;
//import com.medpoint.repository.ProductRepository;
//import com.medpoint.repository.StoreOrderRepository;
//import com.medpoint.repository.TransactionRepository;
//import com.medpoint.service.ReportService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class ReportServiceImpl implements ReportService {
//
//    private static final int LOW_DRUG_STOCK    = 10;
//    private static final int LOW_PRODUCT_STOCK = 5;
//    private static final int EXPIRY_WARN_DAYS  = 30;
//
//    private final TransactionRepository txRepo;
//    private final StoreOrderRepository  storeOrderRepo;
//    private final BookingRepository     bookingRepo;
//    private final DrugRepository        drugRepo;
//    private final ProductRepository     productRepo;
//
//    // ── Main transaction report ───────────────────────────────────────────────
//
//    @Override
//    public ReportResponse generate(ReportRequest req) {
//        Instant from = toInstant(req.getFromDate(), false);
//        Instant to   = toInstant(req.getToDate(),   true);
//
//        List<Transaction> active = txRepo.findAllFiltered(
//                req.getModule(), TransactionStatus.ACTIVE, req.getStaffId(), from, to);
//
//        List<Transaction> cancelled = txRepo.findAllFiltered(
//                req.getModule(), TransactionStatus.CANCELLED, req.getStaffId(), from, to);
//
//        BigDecimal totalRevenue = active.stream()
//                .map(Transaction::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        String groupBy = req.getGroupBy() == null ? "daily" : req.getGroupBy();
//
//        List<GroupRow> groups;
//        List<ItemRow>  topItems = null;
//
//        switch (groupBy) {
//            case "monthly"  -> groups = groupByMonth(active);
//            case "staff"    -> groups = groupByStaff(active);
//            case "category" -> { groups = groupByCategory(active); topItems = topByCategory(active); }
//            case "product"  -> { groups = groupByProduct(active);  topItems = topByProduct(active);  }
//            default         -> groups = groupByDay(active);   // "daily"
//        }
//
//        return ReportResponse.builder()
//                .totalRevenue(totalRevenue)
//                .totalTransactions(active.size())
//                .cancelledCount(cancelled.size())
//                .periodFrom(req.getFromDate())
//                .periodTo(req.getToDate())
//                .groups(groups)
//                .topItems(topItems)
//                .build();
//    }
//
//    // ── Store-order report ────────────────────────────────────────────────────
//
//    @Override
//    public ReportResponse storeOrderReport(LocalDate from, LocalDate to) {
//        LocalDateTime f = from != null ? from.atStartOfDay() : null;
//        LocalDateTime t = to   != null ? to.plusDays(1).atStartOfDay() : null;
//        List<StoreOrder> orders = storeOrderRepo.findByDateRange(f, t);
//
//        BigDecimal totalRevenue = orders.stream()
//                .map(StoreOrder::getTotal)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Group by status
//        Map<String, int[]> statusMap = new HashMap<>(); // [count, revenue×100 sum]
//        Map<String, BigDecimal> statusRev = new HashMap<>();
//        for (StoreOrder o : orders) {
//            String s = o.getStatus() != null ? o.getStatus() : "pending";
//            statusMap.computeIfAbsent(s, k -> new int[]{0})[0]++;
//            statusRev.merge(s, o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO, BigDecimal::add);
//        }
//
//        List<GroupRow> groups = new ArrayList<>();
//        for (Map.Entry<String, int[]> e : statusMap.entrySet()) {
//            String label = capitalize(e.getKey());
//            groups.add(GroupRow.builder()
//                    .label(label)
//                    .count(e.getValue()[0])
//                    .revenue(statusRev.getOrDefault(e.getKey(), BigDecimal.ZERO))
//                    .build());
//        }
//        groups.sort(Comparator.comparing(GroupRow::getLabel));
//
//        return ReportResponse.builder()
//                .totalRevenue(totalRevenue)
//                .totalTransactions(orders.size())
//                .cancelledCount((int) orders.stream().filter(o -> "cancelled".equals(o.getStatus())).count())
//                .periodFrom(from)
//                .periodTo(to)
//                .groups(groups)
//                .build();
//    }
//
//    // ── Hotel / booking report ────────────────────────────────────────────────
//
//    @Override
//    public ReportResponse hotelReport(LocalDate from, LocalDate to) {
//        List<Booking> bookings = bookingRepo.findByCheckInRange(from, to);
//
//        BigDecimal totalRevenue = bookings.stream()
//                .map(b -> b.getTotalCharged() != null ? b.getTotalCharged() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Group by room category name
//        Map<String, int[]>    countMap = new HashMap<>();
//        Map<String, BigDecimal> revMap = new HashMap<>();
//
//        for (Booking b : bookings) {
//            String cat = "Unknown";
//            try { cat = b.getRoom().getCategory().getName(); } catch (Exception ignored) {}
//            countMap.computeIfAbsent(cat, k -> new int[]{0})[0]++;
//            revMap.merge(cat, b.getTotalCharged() != null ? b.getTotalCharged() : BigDecimal.ZERO, BigDecimal::add);
//        }
//
//        List<GroupRow> groups = new ArrayList<>();
//        for (Map.Entry<String, int[]> e : countMap.entrySet()) {
//            groups.add(GroupRow.builder()
//                    .label(e.getKey())
//                    .count(e.getValue()[0])
//                    .revenue(revMap.getOrDefault(e.getKey(), BigDecimal.ZERO))
//                    .build());
//        }
//        groups.sort(Comparator.comparing(GroupRow::getRevenue).reversed());
//
//        return ReportResponse.builder()
//                .totalRevenue(totalRevenue)
//                .totalTransactions(bookings.size())
//                .cancelledCount(0)
//                .periodFrom(from)
//                .periodTo(to)
//                .groups(groups)
//                .build();
//    }
//
//    // ── Inventory report ──────────────────────────────────────────────────────
//
//    @Override
//    public ReportResponse inventoryReport() {
//        LocalDate warnBefore = LocalDate.now().plusDays(EXPIRY_WARN_DAYS);
//
//        List<Drug> drugs = drugRepo.findAll();
//        List<ItemRow> items = new ArrayList<>();
//
//        for (Drug d : drugs) {
//            boolean lowStock  = d.getStock() < LOW_DRUG_STOCK;
//            boolean nearExpiry = d.getExpiryDate() != null && !d.getExpiryDate().isAfter(warnBefore);
//            if (lowStock || nearExpiry) {
//                String label = d.getName();
//                if (nearExpiry) label += " ⚠ exp " + d.getExpiryDate();
//                items.add(ItemRow.builder()
//                        .name(label)
//                        .category(d.getCategory() + " (Drug)")
//                        .totalQty(0)
//                        .totalRevenue(BigDecimal.ZERO)
//                        .stock(d.getStock())
//                        .build());
//            }
//        }
//
//        productRepo.findAll().stream()
//                .filter(p -> p.getStock() < LOW_PRODUCT_STOCK)
//                .forEach(p -> items.add(ItemRow.builder()
//                        .name(p.getName())
//                        .category(p.getCategory() + " (Mart)")
//                        .totalQty(0)
//                        .totalRevenue(BigDecimal.ZERO)
//                        .stock(p.getStock())
//                        .build()));
//
//        items.sort(Comparator.comparingInt(ItemRow::getStock));
//
//        return ReportResponse.builder()
//                .totalRevenue(BigDecimal.ZERO)
//                .totalTransactions(0)
//                .cancelledCount(0)
//                .groups(List.of())
//                .topItems(items)
//                .build();
//    }
//
//    // ── Private grouping helpers ──────────────────────────────────────────────
//
//    private List<GroupRow> groupByDay(List<Transaction> txs) {
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
//        Map<LocalDate, int[]>    countMap = new HashMap<>();
//        Map<LocalDate, BigDecimal> revMap = new HashMap<>();
//
//        for (Transaction t : txs) {
//            LocalDate d = t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
//            countMap.computeIfAbsent(d, k -> new int[]{0})[0]++;
//            revMap.merge(d, t.getAmount(), BigDecimal::add);
//        }
//
//        List<GroupRow> rows = new ArrayList<>();
//        for (Map.Entry<LocalDate, int[]> e : countMap.entrySet()) {
//            rows.add(GroupRow.builder()
//                    .label(e.getKey().format(fmt))
//                    .count(e.getValue()[0])
//                    .revenue(revMap.get(e.getKey()))
//                    .build());
//        }
//        rows.sort(Comparator.comparing(GroupRow::getLabel).reversed());
//        return rows;
//    }
//
//    private List<GroupRow> groupByMonth(List<Transaction> txs) {
//        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
//        Map<String, int[]>    countMap = new HashMap<>();
//        Map<String, BigDecimal> revMap = new HashMap<>();
//
//        for (Transaction t : txs) {
//            String key = t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().format(fmt);
//            countMap.computeIfAbsent(key, k -> new int[]{0})[0]++;
//            revMap.merge(key, t.getAmount(), BigDecimal::add);
//        }
//
//        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getLabel).reversed());
//    }
//
//    private List<GroupRow> groupByStaff(List<Transaction> txs) {
//        Map<String, int[]>    countMap = new HashMap<>();
//        Map<String, BigDecimal> revMap = new HashMap<>();
//
//        for (Transaction t : txs) {
//            String name = t.getStaff() != null ? t.getStaff().getName() : "Unknown";
//            countMap.computeIfAbsent(name, k -> new int[]{0})[0]++;
//            revMap.merge(name, t.getAmount(), BigDecimal::add);
//        }
//
//        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());
//    }
//
//    private List<GroupRow> groupByCategory(List<Transaction> txs) {
//        Map<String, int[]>    countMap = new HashMap<>();
//        Map<String, BigDecimal> revMap = new HashMap<>();
//
//        for (Transaction t : txs) {
//            for (TransactionLineItem li : t.getLineItems()) {
//                String cat = li.getCategory() != null ? li.getCategory() : "Uncategorised";
//                countMap.computeIfAbsent(cat, k -> new int[]{0})[0] += li.getQuantity();
//                revMap.merge(cat, li.getSubtotal(), BigDecimal::add);
//            }
//        }
//
//        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());
//    }
//
//    private List<GroupRow> groupByProduct(List<Transaction> txs) {
//        Map<String, int[]>    countMap = new HashMap<>();
//        Map<String, BigDecimal> revMap = new HashMap<>();
//
//        for (Transaction t : txs) {
//            for (TransactionLineItem li : t.getLineItems()) {
//                String name = li.getName() != null ? li.getName() : "Unknown";
//                countMap.computeIfAbsent(name, k -> new int[]{0})[0] += li.getQuantity();
//                revMap.merge(name, li.getSubtotal(), BigDecimal::add);
//            }
//        }
//
//        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());
//    }
//
//    private List<ItemRow> topByCategory(List<Transaction> txs) {
//        Map<String, int[]>    qtyMap = new HashMap<>();
//        Map<String, BigDecimal> revMap = new HashMap<>();
//
//        for (Transaction t : txs) {
//            for (TransactionLineItem li : t.getLineItems()) {
//                String cat = li.getCategory() != null ? li.getCategory() : "Uncategorised";
//                qtyMap.computeIfAbsent(cat, k -> new int[]{0})[0] += li.getQuantity();
//                revMap.merge(cat, li.getSubtotal(), BigDecimal::add);
//            }
//        }
//
//        return buildTopItems(qtyMap, revMap, null);
//    }
//
//    private List<ItemRow> topByProduct(List<Transaction> txs) {
//        Map<String, int[]>    qtyMap   = new HashMap<>();
//        Map<String, BigDecimal> revMap  = new HashMap<>();
//        Map<String, String>   catMap   = new HashMap<>();
//
//        for (Transaction t : txs) {
//            for (TransactionLineItem li : t.getLineItems()) {
//                String name = li.getName() != null ? li.getName() : "Unknown";
//                qtyMap.computeIfAbsent(name, k -> new int[]{0})[0] += li.getQuantity();
//                revMap.merge(name, li.getSubtotal(), BigDecimal::add);
//                catMap.putIfAbsent(name, li.getCategory());
//            }
//        }
//
//        return buildTopItems(qtyMap, revMap, catMap);
//    }
//
//    private List<ItemRow> buildTopItems(Map<String, int[]> qtyMap, Map<String, BigDecimal> revMap, Map<String, String> catMap) {
//        List<ItemRow> items = new ArrayList<>();
//        for (Map.Entry<String, int[]> e : qtyMap.entrySet()) {
//            String name = e.getKey();
//            items.add(ItemRow.builder()
//                    .name(name)
//                    .category(catMap != null ? catMap.get(name) : null)
//                    .totalQty(e.getValue()[0])
//                    .totalRevenue(revMap.getOrDefault(name, BigDecimal.ZERO))
//                    .build());
//        }
//        items.sort(Comparator.comparing(ItemRow::getTotalRevenue).reversed());
//        return items.size() > 10 ? items.subList(0, 10) : items;
//    }
//
//    private List<GroupRow> toGroupRows(Map<String, int[]> countMap, Map<String, BigDecimal> revMap,
//                                        Comparator<GroupRow> order) {
//        List<GroupRow> rows = new ArrayList<>();
//        for (Map.Entry<String, int[]> e : countMap.entrySet()) {
//            rows.add(GroupRow.builder()
//                    .label(e.getKey())
//                    .count(e.getValue()[0])
//                    .revenue(revMap.getOrDefault(e.getKey(), BigDecimal.ZERO))
//                    .build());
//        }
//        rows.sort(order);
//        return rows;
//    }
//
//    // ── Utilities ─────────────────────────────────────────────────────────────
//
//    private Instant toInstant(LocalDate date, boolean endOfDay) {
//        if (date == null) return null;
//        LocalDate d = endOfDay ? date.plusDays(1) : date;
//        return d.atStartOfDay().toInstant(ZoneOffset.UTC);
//    }
//
//    private String capitalize(String s) {
//        if (s == null || s.isEmpty()) return s;
//        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
//    }
//}




package com.medpoint.service.impl;

import com.medpoint.dto.request.ReportRequest;
import com.medpoint.dto.response.ReportResponse;
import com.medpoint.dto.response.ReportResponse.GroupRow;
import com.medpoint.dto.response.ReportResponse.ItemRow;
import com.medpoint.entity.Booking;
import com.medpoint.entity.Drug;
import com.medpoint.entity.StoreOrder;
import com.medpoint.entity.Transaction;
import com.medpoint.entity.TransactionLineItem;
import com.medpoint.enums.TransactionStatus;
import com.medpoint.enums.TxModule;
import com.medpoint.repository.BookingRepository;
import com.medpoint.repository.DrugRepository;
import com.medpoint.repository.ProductRepository;
import com.medpoint.repository.StoreOrderRepository;
import com.medpoint.repository.TransactionRepository;
import com.medpoint.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int LOW_DRUG_STOCK    = 10;
    private static final int LOW_PRODUCT_STOCK = 5;
    private static final int EXPIRY_WARN_DAYS  = 30;

    private static final DateTimeFormatter DAY_FMT   = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter MONTH_FMT  = DateTimeFormatter.ofPattern("MMM yyyy");

    private final TransactionRepository txRepo;
    private final StoreOrderRepository  storeOrderRepo;
    private final BookingRepository     bookingRepo;
    private final DrugRepository        drugRepo;
    private final ProductRepository     productRepo;

    // ── Main transaction report ───────────────────────────────────────────────

    @Override
    public ReportResponse generate(ReportRequest req) {
        LocalDateTime from = toLocalDateTime(req.getFromDate(), false);
        LocalDateTime to   = toLocalDateTime(req.getToDate(), true);
        // In ReportServiceImpl.generate()
        TxModule module = null;
        if (req.getModule() != null) {
            module = TxModule.valueOf(String.valueOf(req.getModule())); // or however you parse it
        }

        List<Transaction> active    = txRepo.findAllFiltered(module, TransactionStatus.ACTIVE,    req.getStaffId(), from, to);
        List<Transaction> cancelled = txRepo.findAllFiltered(module, TransactionStatus.CANCELLED, req.getStaffId(), from, to);
//        List<Transaction> active    = txRepo.findAllFiltered(String.valueOf(req.getModule()), TransactionStatus.ACTIVE,    req.getStaffId(), from, to);
//        List<Transaction> cancelled = txRepo.findAllFiltered(String.valueOf(req.getModule()), TransactionStatus.CANCELLED, req.getStaffId(), from, to);

        BigDecimal totalRevenue = sumAmount(active);

        String groupBy = req.getGroupBy() == null ? "daily" : req.getGroupBy();

        List<GroupRow> groups;
        List<ItemRow>  topItems = null;

        switch (groupBy) {
            case "monthly"  -> groups = groupByMonth(active);
            case "staff"    -> groups = groupByStaff(active);
            case "category" -> { groups = groupByCategory(active); topItems = topByCategory(active); }
            case "product"  -> { groups = groupByProduct(active);  topItems = topByProduct(active); }
            default         -> groups = groupByDay(active);
        }

        return ReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(active.size())
                .cancelledCount(cancelled.size())
                .periodFrom(req.getFromDate())
                .periodTo(req.getToDate())
                .groups(groups)
                .topItems(topItems)
                .build();
    }

    // ── Store-order report ────────────────────────────────────────────────────

    @Override
    public ReportResponse storeOrderReport(LocalDate from, LocalDate to) {
        List<StoreOrder> orders = storeOrderRepo.findByDateRange(
                toLocalDateTime(from, false),
                toLocalDateTime(to, true));

        BigDecimal totalRevenue = orders.stream()
                .map(StoreOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer>    countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap   = new LinkedHashMap<>();

        for (StoreOrder o : orders) {
            String status = o.getStatus() != null ? o.getStatus() : "pending";
            countMap.merge(status, 1, Integer::sum);
            revMap.merge(status, o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO, BigDecimal::add);
        }

        List<GroupRow> groups = toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getLabel));

        return ReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(orders.size())
                .cancelledCount(countMap.getOrDefault("cancelled", 0))
                .periodFrom(from)
                .periodTo(to)
                .groups(groups)
                .build();
    }

    // ── Hotel / booking report ────────────────────────────────────────────────

    @Override
    public ReportResponse hotelReport(LocalDate from, LocalDate to) {
        List<Booking> bookings = bookingRepo.findByCheckInRange(from, to);

        BigDecimal totalRevenue = bookings.stream()
                .map(b -> b.getTotalCharged() != null ? b.getTotalCharged() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer>    countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap   = new LinkedHashMap<>();

        for (Booking b : bookings) {
            String cat = "Unknown";
            try { cat = b.getRoom().getCategory().getName(); } catch (Exception ignored) {}
            countMap.merge(cat, 1, Integer::sum);
            revMap.merge(cat, b.getTotalCharged() != null ? b.getTotalCharged() : BigDecimal.ZERO, BigDecimal::add);
        }

        List<GroupRow> groups = toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());

        return ReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalTransactions(bookings.size())
                .cancelledCount(0)
                .periodFrom(from)
                .periodTo(to)
                .groups(groups)
                .build();
    }

    // ── Inventory report ──────────────────────────────────────────────────────

    @Override
    public ReportResponse inventoryReport() {
        LocalDate warnBefore = LocalDate.now().plusDays(EXPIRY_WARN_DAYS);
        List<ItemRow> items  = new ArrayList<>();

        for (Drug d : drugRepo.findAll()) {
            boolean lowStock   = d.getStock() < LOW_DRUG_STOCK;
            boolean nearExpiry = d.getExpiryDate() != null && !d.getExpiryDate().isAfter(warnBefore);
            if (!lowStock && !nearExpiry) continue;

            String label = nearExpiry ? d.getName() + " ⚠ exp " + d.getExpiryDate() : d.getName();
            items.add(ItemRow.builder()
                    .name(label)
                    .category(d.getCategory() + " (Drug)")
                    .totalQty(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .stock(d.getStock())
                    .build());
        }

        productRepo.findAll().stream()
                .filter(p -> p.getStock() < LOW_PRODUCT_STOCK)
                .forEach(p -> items.add(ItemRow.builder()
                        .name(p.getName())
                        .category(p.getCategory() + " (Mart)")
                        .totalQty(0)
                        .totalRevenue(BigDecimal.ZERO)
                        .stock(p.getStock())
                        .build()));

        items.sort(Comparator.comparingInt(ItemRow::getStock));

        return ReportResponse.builder()
                .totalRevenue(BigDecimal.ZERO)
                .totalTransactions(0)
                .cancelledCount(0)
                .groups(List.of())
                .topItems(items)
                .build();
    }

    // ── Grouping helpers ──────────────────────────────────────────────────────

    private List<GroupRow> groupByDay(List<Transaction> txs) {
        // Key by LocalDate so we can sort correctly, format only for display
        Map<LocalDate, Integer>     countMap = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal>  revMap   = new LinkedHashMap<>();

        for (Transaction t : txs) {
            LocalDate d = t.getCreatedAt().toLocalDate();
            countMap.merge(d, 1, Integer::sum);
            revMap.merge(d, t.getAmount(), BigDecimal::add);
        }

        List<GroupRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> e : countMap.entrySet()) {
            rows.add(GroupRow.builder()
                    .label(e.getKey().format(DAY_FMT))
                    .count(e.getValue())
                    .revenue(revMap.get(e.getKey()))
                    .build());
        }
        // Sort by actual date descending before converting key to string
        rows.sort(Comparator.comparing(GroupRow::getLabel, Comparator.reverseOrder()));
        // Re-sort properly: rebuild with date key for sorting
        List<Map.Entry<LocalDate, Integer>> sorted = new ArrayList<>(countMap.entrySet());
        sorted.sort(Map.Entry.<LocalDate, Integer>comparingByKey().reversed());
        rows.clear();
        for (Map.Entry<LocalDate, Integer> e : sorted) {
            rows.add(GroupRow.builder()
                    .label(e.getKey().format(DAY_FMT))
                    .count(e.getValue())
                    .revenue(revMap.get(e.getKey()))
                    .build());
        }
        return rows;
    }

    private List<GroupRow> groupByMonth(List<Transaction> txs) {
        Map<String, Integer>    countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap   = new LinkedHashMap<>();

        for (Transaction t : txs) {
            String key = t.getCreatedAt().toLocalDate().format(MONTH_FMT);
            countMap.merge(key, 1, Integer::sum);
            revMap.merge(key, t.getAmount(), BigDecimal::add);
        }

        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getLabel).reversed());
    }

    private List<GroupRow> groupByStaff(List<Transaction> txs) {
        Map<String, Integer>    countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap   = new LinkedHashMap<>();

        for (Transaction t : txs) {
            String name = t.getStaff() != null ? t.getStaff().getName() : "Unknown";
            countMap.merge(name, 1, Integer::sum);
            revMap.merge(name, t.getAmount(), BigDecimal::add);
        }

        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());
    }

    private List<GroupRow> groupByCategory(List<Transaction> txs) {
        Map<String, Integer>    countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap   = new LinkedHashMap<>();

        for (Transaction t : txs) {
            for (TransactionLineItem li : t.getLineItems()) {
                String cat = li.getCategory() != null ? li.getCategory() : "Uncategorised";
                countMap.merge(cat, li.getQuantity(), Integer::sum);
                revMap.merge(cat, li.getSubtotal(), BigDecimal::add);
            }
        }

        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());
    }

    private List<GroupRow> groupByProduct(List<Transaction> txs) {
        Map<String, Integer>    countMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap   = new LinkedHashMap<>();

        for (Transaction t : txs) {
            for (TransactionLineItem li : t.getLineItems()) {
                String name = li.getName() != null ? li.getName() : "Unknown";
                countMap.merge(name, li.getQuantity(), Integer::sum);
                revMap.merge(name, li.getSubtotal(), BigDecimal::add);
            }
        }

        return toGroupRows(countMap, revMap, Comparator.comparing(GroupRow::getRevenue).reversed());
    }

    // ── Top-item helpers ──────────────────────────────────────────────────────

    private List<ItemRow> topByCategory(List<Transaction> txs) {
        Map<String, Integer>    qtyMap = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap = new LinkedHashMap<>();

        for (Transaction t : txs) {
            for (TransactionLineItem li : t.getLineItems()) {
                String cat = li.getCategory() != null ? li.getCategory() : "Uncategorised";
                qtyMap.merge(cat, li.getQuantity(), Integer::sum);
                revMap.merge(cat, li.getSubtotal(), BigDecimal::add);
            }
        }

        return buildTopItems(qtyMap, revMap, null);
    }

    private List<ItemRow> topByProduct(List<Transaction> txs) {
        Map<String, Integer>    qtyMap  = new LinkedHashMap<>();
        Map<String, BigDecimal> revMap  = new LinkedHashMap<>();
        Map<String, String>     catMap  = new LinkedHashMap<>();

        for (Transaction t : txs) {
            for (TransactionLineItem li : t.getLineItems()) {
                String name = li.getName() != null ? li.getName() : "Unknown";
                qtyMap.merge(name, li.getQuantity(), Integer::sum);
                revMap.merge(name, li.getSubtotal(), BigDecimal::add);
                catMap.putIfAbsent(name, li.getCategory());
            }
        }

        return buildTopItems(qtyMap, revMap, catMap);
    }

    private List<ItemRow> buildTopItems(Map<String, Integer> qtyMap,
                                        Map<String, BigDecimal> revMap,
                                        Map<String, String> catMap) {
        List<ItemRow> items = new ArrayList<>();
        for (Map.Entry<String, Integer> e : qtyMap.entrySet()) {
            String name = e.getKey();
            items.add(ItemRow.builder()
                    .name(name)
                    .category(catMap != null ? catMap.get(name) : null)
                    .totalQty(e.getValue())
                    .totalRevenue(revMap.getOrDefault(name, BigDecimal.ZERO))
                    .build());
        }
        items.sort(Comparator.comparing(ItemRow::getTotalRevenue).reversed());
        return items.size() > 10 ? items.subList(0, 10) : items;
    }

    // ── Shared row builder ────────────────────────────────────────────────────

    private List<GroupRow> toGroupRows(Map<String, Integer> countMap,
                                       Map<String, BigDecimal> revMap,
                                       Comparator<GroupRow> order) {
        List<GroupRow> rows = new ArrayList<>();
        for (Map.Entry<String, Integer> e : countMap.entrySet()) {
            rows.add(GroupRow.builder()
                    .label(e.getKey())
                    .count(e.getValue())
                    .revenue(revMap.getOrDefault(e.getKey(), BigDecimal.ZERO))
                    .build());
        }
        rows.sort(order);
        return rows;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private LocalDateTime toLocalDateTime(LocalDate date, boolean endOfDay) {
        if (date == null) return null;
        return endOfDay ? date.plusDays(1).atStartOfDay() : date.atStartOfDay();
    }

    private BigDecimal sumAmount(List<Transaction> txs) {
        return txs.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}