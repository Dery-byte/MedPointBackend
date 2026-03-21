package com.medpoint.service.impl;
import com.medpoint.dto.request.TransactionFilterRequest;
import com.medpoint.dto.response.*;
import com.medpoint.entity.Transaction;
import com.medpoint.enums.TxModule;
import com.medpoint.repository.*;
import com.medpoint.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int EXPIRY_WARN_DAYS    = 90;

    private static final Map<TxModule, Double> COST_RATIOS = Map.of(
            TxModule.DRUGSTORE,  0.55,
            TxModule.MART,       0.65,
            TxModule.HOTEL,      0.40,
            TxModule.RESTAURANT, 0.50
    );

    private final TransactionRepository txRepo;
    private final UserRepository userRepo;
    private final DrugRepository drugRepo;
    private final ProductRepository productRepo;
    private final RoomRepository roomRepo;
    private final RestaurantTableRepository tableRepo;

    @Override
    public DashboardResponse  getDashboard() {
        // Today's revenue
        Instant startOfDay = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay   = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        BigDecimal todayRevenue = txRepo.findFiltered(null, null, null, startOfDay, endOfDay)
                .stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = txRepo.sumAllActive();
        long activeStaff = userRepo.findByActiveTrue().size();

        // Low stock count (drugs + products)
        int lowDrugs    = drugRepo.findLowStock(LOW_STOCK_THRESHOLD).size();
        int lowProducts = productRepo.findLowStock(LOW_STOCK_THRESHOLD).size();
        int lowStockCount = lowDrugs + lowProducts;

        // Revenue by module — keys are lowercase to match frontend revenueByModule[m] lookup
        Map<String, BigDecimal> revenueByModule = new LinkedHashMap<>();
        for (TxModule m : TxModule.values()) {
            revenueByModule.put(m.name().toLowerCase(), txRepo.sumActiveByModule(m));
        }

        // Recent transactions (last 10, with staff name)
        List<DashboardResponse.RecentTx> recentTxs = txRepo
                .findFiltered(null, null, null, null, null).stream()
                .limit(10)
                .map(t -> DashboardResponse.RecentTx.builder()
                        .id(t.getId())
                        .staff(t.getStaff().getName())
                        .module(t.getModule().name())
                        .amount(t.getAmount())
                        .description(t.getDescription())
                        .status(t.getStatus().name())
                        .date(DateTimeFormatter.ISO_INSTANT.format(t.getCreatedAt()))
                        .build())
                .toList();

        return DashboardResponse.builder()
                .todayRevenue(todayRevenue)
                .totalRevenue(totalRevenue)
                .activeStaff(activeStaff)
                .lowStockCount(lowStockCount)
                .revenueByModule(revenueByModule)
                .recentTransactions(recentTxs)
                .build();
    }

    @Override
    public RevenueResponse getRevenue(TransactionFilterRequest filter) {
        List<Transaction> txList = txRepo.findFiltered(
                filter.getModule(),
                filter.getStaffId() != null ? userRepo.findById(filter.getStaffId()).orElse(null) : null,
                filter.getStatus(),
                filter.getFromDate() != null ? filter.getFromDate().atStartOfDay().toInstant(ZoneOffset.UTC) : null,
                filter.getToDate()   != null ? filter.getToDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null
        );

        BigDecimal totalRevenue = txList.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost    = txList.stream()
                .map(t -> t.getAmount().multiply(BigDecimal.valueOf(COST_RATIOS.getOrDefault(t.getModule(), 0.5))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = totalRevenue.subtract(totalCost);
        double margin = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;

        List<RevenueResponse.ModuleSummary> byModule = Arrays.stream(TxModule.values()).map(m -> {
            List<Transaction> modTxs = txList.stream().filter(t -> t.getModule() == m).toList();
            BigDecimal rev  = modTxs.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cost = rev.multiply(BigDecimal.valueOf(COST_RATIOS.getOrDefault(m, 0.5)));
            BigDecimal prof = rev.subtract(cost);
            double mod_margin = rev.compareTo(BigDecimal.ZERO) > 0
                    ? prof.divide(rev, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
            return RevenueResponse.ModuleSummary.builder()
                    .module(m.name().toLowerCase())
                    .revenue(rev).cost(cost).profit(prof)
                    .marginPercent(mod_margin).transactionCount(modTxs.size()).build();
        }).toList();

        Map<Long, List<Transaction>> byStaffMap = txList.stream()
                .collect(Collectors.groupingBy(t -> t.getStaff().getId()));
        List<RevenueResponse.StaffSummary> byStaff = byStaffMap.entrySet().stream().map(e -> {
            List<Transaction> staffTxs = e.getValue();
            BigDecimal rev  = staffTxs.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cost = staffTxs.stream()
                    .map(t -> t.getAmount().multiply(BigDecimal.valueOf(COST_RATIOS.getOrDefault(t.getModule(), 0.5))))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return RevenueResponse.StaffSummary.builder()
                    .staffId(e.getKey()).staffName(staffTxs.get(0).getStaff().getName())
                    .revenue(rev).cost(cost).profit(rev.subtract(cost))
                    .transactionCount(staffTxs.size()).build();
        }).sorted(Comparator.comparing(RevenueResponse.StaffSummary::getRevenue).reversed()).toList();

        return RevenueResponse.builder()
                .totalRevenue(totalRevenue).totalCost(totalCost)
                .totalProfit(totalProfit).marginPercent(margin)
                .byModule(byModule).byStaff(byStaff).byPeriod(List.of())
                .build();
    }

    @Override
    public StockAlertResponse getStockAlerts() {
        LocalDate in90 = LocalDate.now().plusDays(EXPIRY_WARN_DAYS);
        var lowDrugs      = drugRepo.findLowStock(LOW_STOCK_THRESHOLD);
        var expiringDrugs = drugRepo.findExpiringSoon(in90);
        var lowProducts   = productRepo.findLowStock(LOW_STOCK_THRESHOLD);

        return StockAlertResponse.builder()
                .lowStockDrugs(lowDrugs.stream().map(d -> StockAlertResponse.DrugAlert.builder()
                        .id(d.getId()).name(d.getName()).category(d.getCategory())
                        .stock(d.getStock()).price(d.getPrice()).expiryDate(d.getExpiryDate())
                        .expiryStatus(expiryStatus(d)).build()).toList())
                .expiringDrugs(expiringDrugs.stream().map(d -> StockAlertResponse.DrugAlert.builder()
                        .id(d.getId()).name(d.getName()).category(d.getCategory())
                        .stock(d.getStock()).price(d.getPrice()).expiryDate(d.getExpiryDate())
                        .expiryStatus(expiryStatus(d)).build()).toList())
                .lowStockProducts(lowProducts.stream().map(p -> StockAlertResponse.ProductAlert.builder()
                        .id(p.getId()).name(p.getName()).category(p.getCategory())
                        .stock(p.getStock()).price(p.getPrice()).build()).toList())
                .build();
    }

    private String expiryStatus(com.medpoint.entity.Drug d) {
        if (d.getExpiryDate() == null) return "NONE";
        LocalDate today = LocalDate.now();
        if (d.getExpiryDate().isBefore(today)) return "EXPIRED";
        if (d.getExpiryDate().isBefore(today.plusDays(30))) return "EXPIRING_IMMINENT";
        if (d.getExpiryDate().isBefore(today.plusDays(EXPIRY_WARN_DAYS))) return "EXPIRING_SOON";
        return "OK";
    }
}
