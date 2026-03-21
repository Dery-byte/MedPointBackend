package com.medpoint.service.impl;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.*;
import com.medpoint.enums.*;
import com.medpoint.exception.*;
import com.medpoint.repository.*;
import com.medpoint.service.MartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MartServiceImpl implements MartService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepo.findByActiveTrueOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return toResponse(findProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        Product p = Product.builder()
                .name(req.getName()).category(req.getCategory())
                .price(req.getPrice()).stock(req.getStock()).build();
        return toResponse(productRepo.save(p));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product p = findProduct(id);
        p.setName(req.getName()); p.setCategory(req.getCategory());
        p.setPrice(req.getPrice()); p.setStock(req.getStock());
        return toResponse(productRepo.save(p));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product p = findProduct(id);
        p.setActive(false);
        productRepo.save(p);
    }

    @Override
    @Transactional
    public ProductResponse restockProduct(Long id, RestockRequest req) {
        Product p = findProduct(id);
        p.setStock(p.getStock() + req.getQuantity());
        return toResponse(productRepo.save(p));
    }

    @Override
    @Transactional
    public ProductResponse updateProductPrice(Long id, PriceUpdateRequest req) {
        Product p = findProduct(id);
        p.setPrice(req.getPrice());
        return toResponse(productRepo.save(p));
    }

    @Override
    public List<String> getCategories() {
        return productRepo.findDistinctCategories();
    }



    @Override
    @Transactional
    public TransactionResponse checkout(MartCheckoutRequest req, Long staffId) {
        User staff = userRepo.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", staffId));

        List<TransactionLineItem> lineItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (MartCheckoutRequest.CartItem ci : req.getItems()) {
            Product p = findProduct(ci.getProductId());
            if (p.getStock() < ci.getQuantity()) {
                throw new BusinessException("Insufficient stock for: " + p.getName()
                        + " (available: " + p.getStock() + ")");
            }
            p.setStock(p.getStock() - ci.getQuantity());
            productRepo.save(p);

            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(subtotal);
            lineItems.add(TransactionLineItem.builder()
                    .name(p.getName()).category(p.getCategory())
                    .kind(LineItemKind.ITEM).quantity(ci.getQuantity())
                    .unitPrice(p.getPrice()).subtotal(subtotal).build());
        }

        String ref = "MRT-" + (txRepo.count() + 1001);
        String desc = "Mart Sale (" + req.getItems().size() + " item" + (req.getItems().size() != 1 ? "s)" : ")");
        Transaction tx = Transaction.builder()
                .reference(ref).module(TxModule.MART).amount(total)
                .staff(staff).description(desc).build();
        lineItems.forEach(li -> li.setTransaction(tx));
        tx.setLineItems(lineItems);

        Transaction saved = txRepo.save(tx);
        return toTxResponse(saved);
    }

    private Product findProduct(Long id) {
        return productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).category(p.getCategory())
                .price(p.getPrice()).stock(p.getStock()).active(p.isActive())
                .lowStock(p.getStock() <= LOW_STOCK_THRESHOLD).build();
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
                .createdAt(t.getCreatedAt()).build();
    }
}
