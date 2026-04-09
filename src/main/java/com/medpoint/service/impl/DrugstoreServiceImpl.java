package com.medpoint.service.impl;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.*;
import com.medpoint.enums.*;
import com.medpoint.exception.*;
import com.medpoint.repository.*;
import com.medpoint.service.DrugstoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class DrugstoreServiceImpl implements DrugstoreService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int EXPIRY_WARN_DAYS    = 90;
    private static final int EXPIRY_CRITICAL_DAYS = 30;

    private final DrugRepository drugRepo;
    private final MedicalServiceRepository svcRepo;
    private final NonDrugItemRepository ndRepo;
    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    // ── Drugs ─────────────────────────────────────────────────────────────────

    @Override
    public List<DrugResponse> getAllDrugs() {
        return drugRepo.findByActiveTrueOrderByNameAsc().stream().map(this::toDrugResponse).toList();
    }

    @Override
    public DrugResponse getDrugById(Long id) {
        return toDrugResponse(findDrug(id));
    }

    @Override
    @Transactional
    public DrugResponse createDrug(DrugRequest req) {
        Drug drug = Drug.builder()
                .name(req.getName()).category(req.getCategory())
                .price(req.getPrice()).costPrice(req.getCostPrice())
                .stock(req.getStock()).expiryDate(req.getExpiryDate()).build();
        return toDrugResponse(drugRepo.save(drug));
    }

    @Override
    @Transactional
    public DrugResponse updateDrug(Long id, DrugRequest req) {
        Drug drug = findDrug(id);
        drug.setName(req.getName()); drug.setCategory(req.getCategory());
        drug.setPrice(req.getPrice()); drug.setCostPrice(req.getCostPrice());
        drug.setStock(req.getStock()); drug.setExpiryDate(req.getExpiryDate());
        return toDrugResponse(drugRepo.save(drug));
    }

    @Override
    @Transactional
    public void deleteDrug(Long id) {
        Drug drug = findDrug(id);
        drug.setActive(false);   // Soft delete
        drugRepo.save(drug);
    }

    @Override
    @Transactional
    public DrugResponse restockDrug(Long id, RestockRequest req) {
        Drug drug = findDrug(id);
        drug.setStock(drug.getStock() + req.getQuantity());
        return toDrugResponse(drugRepo.save(drug));
    }

    @Override
    @Transactional
    public DrugResponse updateDrugPrice(Long id, PriceUpdateRequest req) {
        Drug drug = findDrug(id);
        drug.setPrice(req.getPrice());
        return toDrugResponse(drugRepo.save(drug));
    }

    // ── Medical Services ──────────────────────────────────────────────────────

    @Override
    public List<MedicalServiceResponse> getAllServices() {
        return svcRepo.findByActiveTrueOrderByNameAsc().stream().map(this::toSvcResponse).toList();
    }

    @Override
    @Transactional
    public MedicalServiceResponse createService(MedicalServiceRequest req) {
        MedicalService svc = MedicalService.builder()
                .name(req.getName()).category(req.getCategory())
                .price(req.getPrice()).costPrice(req.getCostPrice()).build();
        return toSvcResponse(svcRepo.save(svc));
    }

    @Override
    @Transactional
    public MedicalServiceResponse updateService(Long id, MedicalServiceRequest req) {
        MedicalService svc = findService(id);
        svc.setName(req.getName()); svc.setCategory(req.getCategory());
        svc.setPrice(req.getPrice()); svc.setCostPrice(req.getCostPrice());
        return toSvcResponse(svcRepo.save(svc));
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        MedicalService svc = findService(id);
        svc.setActive(false);
        svcRepo.save(svc);
    }

    @Override
    @Transactional
    public MedicalServiceResponse updateServicePrice(Long id, PriceUpdateRequest req) {
        MedicalService svc = findService(id);
        svc.setPrice(req.getPrice());
        return toSvcResponse(svcRepo.save(svc));
    }

    // ── Non-Drug Items ────────────────────────────────────────────────────────

    @Override
    public List<NonDrugItemResponse> getAllNonDrugItems() {
        return ndRepo.findByActiveTrueOrderByNameAsc().stream().map(this::toNdResponse).toList();
    }

    @Override
    @Transactional
    public NonDrugItemResponse createNonDrugItem(NonDrugItemRequest req) {
        NonDrugItem item = NonDrugItem.builder()
                .name(req.getName()).category(req.getCategory())
                .price(req.getPrice()).costPrice(req.getCostPrice())
                .stock(req.getStock()).build();
        return toNdResponse(ndRepo.save(item));
    }

    @Override
    @Transactional
    public NonDrugItemResponse updateNonDrugItem(Long id, NonDrugItemRequest req) {
        NonDrugItem item = findNonDrug(id);
        item.setName(req.getName()); item.setCategory(req.getCategory());
        item.setPrice(req.getPrice()); item.setCostPrice(req.getCostPrice());
        item.setStock(req.getStock());
        return toNdResponse(ndRepo.save(item));
    }

    @Override
    @Transactional
    public void deleteNonDrugItem(Long id) {
        NonDrugItem item = findNonDrug(id);
        item.setActive(false);
        ndRepo.save(item);
    }

    @Override
    @Transactional
    public NonDrugItemResponse updateNonDrugItemPrice(Long id, PriceUpdateRequest req) {
        NonDrugItem item = findNonDrug(id);
        item.setPrice(req.getPrice());
        return toNdResponse(ndRepo.save(item));
    }

    // ── Bulk Operations ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<DrugResponse> bulkCreateDrugs(List<DrugRequest> drugs) {
        return drugs.stream().map(this::createDrug).toList();
    }

    // ── Dispense Operations ───────────────────────────────────────────────────

    @Override
    @Transactional
    public TransactionResponse dispense(DrugDispenseRequest req, Long staffId) {
        User staff = findStaff(staffId);
        List<TransactionLineItem> lineItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder desc = new StringBuilder("Drug Dispense (");
        int count = 0;

        for (DrugDispenseRequest.DrugLineItem item : req.getItems()) {
            Drug drug = findDrug(item.getDrugId());
            if (drug.getStock() < item.getQuantity()) {
                throw new BusinessException("Insufficient stock for: " + drug.getName()
                        + " (available: " + drug.getStock() + ")");
            }
            drug.setStock(drug.getStock() - item.getQuantity());
            drugRepo.save(drug);

            BigDecimal subtotal = drug.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            count++;

            lineItems.add(TransactionLineItem.builder()
                    .name(drug.getName()).category(drug.getCategory())
                    .kind(LineItemKind.ITEM).quantity(item.getQuantity())
                    .unitPrice(drug.getPrice()).subtotal(subtotal).build());
        }
        desc.append(count).append(" item").append(count != 1 ? "s)" : ")");

        return saveTransaction(TxModule.DRUGSTORE, total, staff, desc.toString(), lineItems);
    }

    @Override
    @Transactional
    public TransactionResponse dispenseService(ServiceDispenseRequest req, Long staffId) {
        User staff = findStaff(staffId);
        List<TransactionLineItem> lineItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Services rendered
        List<String> svcNames = new ArrayList<>();
        for (Long svcId : req.getServiceIds()) {
            MedicalService svc = findService(svcId);
            BigDecimal subtotal = svc.getPrice();
            total = total.add(subtotal);
            svcNames.add(svc.getName());
            lineItems.add(TransactionLineItem.builder()
                    .name(svc.getName()).category(svc.getCategory())
                    .kind(LineItemKind.SERVICE).quantity(1)
                    .unitPrice(svc.getPrice()).subtotal(subtotal).build());
        }

        // Prescribed items (drugs or non-drug items)
        if (req.getItems() != null) {
            for (ServiceDispenseRequest.ItemLineItem item : req.getItems()) {
                BigDecimal unitPrice;
                String name, category;
                if (item.isDrug()) {
                    Drug drug = findDrug(item.getItemId());
                    if (drug.getStock() < item.getQuantity()) {
                        throw new BusinessException("Insufficient stock for: " + drug.getName());
                    }
                    drug.setStock(drug.getStock() - item.getQuantity());
                    drugRepo.save(drug);
                    unitPrice = drug.getPrice(); name = drug.getName(); category = drug.getCategory();
                } else {
                    NonDrugItem nd = findNonDrug(item.getItemId());
                    if (nd.getStock() > 0 && nd.getStock() < item.getQuantity()) {
                        throw new BusinessException("Insufficient stock for: " + nd.getName());
                    }
                    if (nd.getStock() > 0) {
                        nd.setStock(nd.getStock() - item.getQuantity());
                        ndRepo.save(nd);
                    }
                    unitPrice = nd.getPrice(); name = nd.getName(); category = nd.getCategory();
                }
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(subtotal);
                lineItems.add(TransactionLineItem.builder()
                        .name(name).category(category).kind(LineItemKind.ITEM)
                        .quantity(item.getQuantity()).unitPrice(unitPrice).subtotal(subtotal).build());
            }
        }

        String desc = "Services: " + String.join(", ", svcNames);
        return saveTransaction(TxModule.DRUGSTORE, total, staff, desc, lineItems);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private TransactionResponse saveTransaction(TxModule module, BigDecimal amount, User staff,
                                                String desc, List<TransactionLineItem> lineItems) {
        String ref = generateRef();
        Transaction tx = Transaction.builder()
                .reference(ref).module(module).amount(amount)
                .staff(staff).description(desc).build();
        lineItems.forEach(li -> li.setTransaction(tx));
        tx.setLineItems(lineItems);
        Transaction saved = txRepo.save(tx);
        return toTxResponse(saved);
    }

    private String generateRef() {
        long count = txRepo.count() + 1001;
        return "DRG-" + count;
    }

    private User findStaff(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
    private Drug findDrug(Long id) {
        return drugRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Drug", id));
    }
    private MedicalService findService(Long id) {
        return svcRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("MedicalService", id));
    }
    private NonDrugItem findNonDrug(Long id) {
        return ndRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("NonDrugItem", id));
    }

    private String expiryStatus(Drug d) {
        if (d.getExpiryDate() == null) return "NONE";
        LocalDate today = LocalDate.now();
        if (d.getExpiryDate().isBefore(today)) return "EXPIRED";
        if (d.getExpiryDate().isBefore(today.plusDays(EXPIRY_CRITICAL_DAYS))) return "EXPIRING_IMMINENT";
        if (d.getExpiryDate().isBefore(today.plusDays(EXPIRY_WARN_DAYS))) return "EXPIRING_SOON";
        return "OK";
    }

    DrugResponse toDrugResponse(Drug d) {
        return DrugResponse.builder()
                .id(d.getId()).name(d.getName()).category(d.getCategory())
                .price(d.getPrice()).costPrice(d.getCostPrice())
                .stock(d.getStock()).expiryDate(d.getExpiryDate())
                .active(d.isActive()).lowStock(d.getStock() <= LOW_STOCK_THRESHOLD)
                .expiryStatus(expiryStatus(d)).build();
    }
    MedicalServiceResponse toSvcResponse(MedicalService s) {
        return MedicalServiceResponse.builder()
                .id(s.getId()).name(s.getName()).category(s.getCategory())
                .price(s.getPrice()).costPrice(s.getCostPrice()).active(s.isActive()).build();
    }
    NonDrugItemResponse toNdResponse(NonDrugItem n) {
        return NonDrugItemResponse.builder()
                .id(n.getId()).name(n.getName()).category(n.getCategory())
                .price(n.getPrice()).costPrice(n.getCostPrice())
                .stock(n.getStock()).active(n.isActive()).build();
    }
    TransactionResponse toTxResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).reference(t.getReference()).module(t.getModule())
                .amount(t.getAmount()).staffName(t.getStaff().getName())
                .description(t.getDescription()).status(t.getStatus())
                .lineItems(t.getLineItems().stream().map(li -> TransactionResponse.LineItemDto.builder()
                        .id(li.getId()).name(li.getName()).category(li.getCategory())
                        .kind(li.getKind()).quantity(li.getQuantity())
                        .unitPrice(li.getUnitPrice()).subtotal(li.getSubtotal()).build()).toList())
                .cancelledByName(t.getCancelledBy() != null ? t.getCancelledBy().getName() : null)
                .cancelledAt(t.getCancelledAt()).createdAt(Instant.from(t.getCreatedAt())).build();
    }
}
