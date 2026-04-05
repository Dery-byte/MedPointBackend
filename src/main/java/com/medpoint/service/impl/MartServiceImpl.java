package com.medpoint.service.impl;

import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.*;
import com.medpoint.enums.*;
import com.medpoint.exception.*;
import com.medpoint.repository.*;
import com.medpoint.service.MartService;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MartServiceImpl implements MartService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080/api}")
    private String baseUrl;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepo.findAll().stream().map(this::toResponse).toList();
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
                .price(req.getPrice()).costPrice(req.getCostPrice())
                .stock(req.getStock())
                .imageUrl(req.getImageUrl())
                .featured(req.isFeatured())
                .discount(req.getDiscount())
                .onSale(req.isOnSale())
                .showOnStore(req.isShowOnStore())
                .description(req.getDescription())
                .tags(req.getTags())
                .variations(req.getVariations())
                .build();
        return toResponse(productRepo.save(p));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product p = findProduct(id);
        p.setName(req.getName()); p.setCategory(req.getCategory());
        p.setPrice(req.getPrice()); p.setStock(req.getStock());
        p.setCostPrice(req.getCostPrice());
        p.setFeatured(req.isFeatured());
        p.setDiscount(req.getDiscount());
        p.setOnSale(req.isOnSale());
        p.setShowOnStore(req.isShowOnStore());
        p.setDescription(req.getDescription());
        p.setTags(req.getTags());
        p.setVariations(req.getVariations());
        // Only overwrite imageUrl if explicitly provided in request
        if (req.getImageUrl() != null) {
            p.setImageUrl(req.getImageUrl());
        }
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
    @Transactional
    public String uploadProductImage(Long productId, MultipartFile file) {

        Product product = findProduct(productId);

        try {

            Path uploadPath = Paths.get(uploadDir, "products")
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadPath);

            String filenameBase = UUID.randomUUID().toString();

            String mainFilename = filenameBase + ".webp";
            String thumbFilename = filenameBase + "_thumb.webp";

            Path mainPath = uploadPath.resolve(mainFilename);
            Path thumbPath = uploadPath.resolve(thumbFilename);

            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            if (originalImage == null) {
                throw new BusinessException("Invalid image file");
            }

            // Save main optimized image (max width 800px)
            Thumbnails.of(originalImage)
                    .size(800, 800)
                    .outputFormat("webp")
                    .outputQuality(0.8)
                    .toFile(mainPath.toFile());

            // Save thumbnail version (300px)
            Thumbnails.of(originalImage)
                    .size(300, 300)
                    .outputFormat("webp")
                    .outputQuality(0.75)
                    .toFile(thumbPath.toFile());

            String imageUrl = "/uploads/products/" + mainFilename;
            String thumbUrl = "/uploads/products/" + thumbFilename;

            product.setImageUrl(imageUrl);
            product.setThumbnailUrl(thumbUrl);

            productRepo.save(product);

            return imageUrl;

        } catch (IOException e) {
            throw new BusinessException("Failed to store image: " + e.getMessage());
        }
    }

//    @Override
//    @Transactional
//    public String uploadProductImage(Long productId, MultipartFile file) {
//
//        Product product = findProduct(productId);
//
//        try {
//
//            Path uploadPath = Paths.get(uploadDir, "products")
//                    .toAbsolutePath()
//                    .normalize();
//
//            Files.createDirectories(uploadPath);
//
//            String filenameBase = UUID.randomUUID().toString();
//
//            String mainFilename = filenameBase + ".jpg";
//            String thumbFilename = filenameBase + "_thumb.jpg";
//
//            Path mainPath = uploadPath.resolve(mainFilename);
//            Path thumbPath = uploadPath.resolve(thumbFilename);
//
//            BufferedImage originalImage = ImageIO.read(file.getInputStream());
//
//            if (originalImage == null) {
//                throw new BusinessException("Invalid image file");
//            }
//
//            // Save main image
//            Thumbnails.of(originalImage)
//                    .size(800, 800)
//                    .outputQuality(0.8)
//                    .toFile(mainPath.toFile());
//
//            // Save thumbnail
//            Thumbnails.of(originalImage)
//                    .size(300, 300)
//                    .outputQuality(0.8)
//                    .toFile(thumbPath.toFile());
//
//            String imageUrl = "/uploads/products/" + mainFilename;
//            String thumbUrl = "/uploads/products/" + thumbFilename;
//
//            product.setImageUrl(imageUrl);
//            product.setThumbnailUrl(thumbUrl);
//
//            productRepo.save(product);
//
//            return imageUrl;
//
//        } catch (IOException e) {
//            throw new BusinessException("Failed to store image: " + e.getMessage());
//        }
//    }
//    @Override
//    @Transactional
//    public String uploadProductImage(Long productId, MultipartFile file) {
//        Product p = findProduct(productId);
//        try {
//            Path uploadPath = Paths.get(uploadDir, "products").toAbsolutePath().normalize();
//            Files.createDirectories(uploadPath);
//            String originalFilename = StringUtils.cleanPath(
//                file.getOriginalFilename() != null ? file.getOriginalFilename() : "image"
//            );
//            String filename = UUID.randomUUID() + "_" + originalFilename;
//            Path targetPath = uploadPath.resolve(filename);
//            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
//            String imageUrl = "/uploads/products/" + filename;
//            p.setImageUrl(imageUrl);
//            productRepo.save(p);
//            return imageUrl;
//        } catch (IOException e) {
//            throw new BusinessException("Failed to store image: " + e.getMessage());
//        }
//    }

    @Override
    @Transactional
    public List<ProductResponse> bulkCreateProducts(List<ProductRequest> products) {
        return products.stream().map(this::createProduct).toList();
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
                .price(p.getPrice()).stock(p.getStock()).active(p.isActive()).costPrice(p.getCostPrice())
                .lowStock(p.getStock() <= LOW_STOCK_THRESHOLD)
                .imageUrl(p.getImageUrl())
                .featured(p.isFeatured())
                .discount(p.getDiscount())
                .onSale(p.isOnSale())
                .showOnStore(p.isShowOnStore())
                .description(p.getDescription())
                .tags(p.getTags())
                .variations(p.getVariations())
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
                .createdAt(t.getCreatedAt()).build();
    }



















    @Override
    public List<ProductResponse> uploadProductsFromExcel(MultipartFile file) {
        List<ProductRequest> requests = new ArrayList<>();
        try (
                InputStream inputStream = file.getInputStream();
                Workbook workbook = WorkbookFactory.create(inputStream)
        ) {
            Sheet sheet = workbook.getSheet("Products");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            // skip header
            if (rows.hasNext()) rows.next();
            while (rows.hasNext()) {
                Row row = rows.next();
                BigDecimal price     = getDecimal(row.getCell(2));
                BigDecimal costPrice = getDecimal(row.getCell(3));
                if (costPrice.compareTo(BigDecimal.ZERO) == 0) costPrice = price;
                ProductRequest request = ProductRequest.builder()
                        .name(getString(row.getCell(0)))
                        .category(getString(row.getCell(1)))
                        .price(price)
                        .costPrice(costPrice)
                        .stock(getInt(row.getCell(4)))
                        .featured(false)
                        .onSale(false)
                        .showOnStore(true)
                        .build();

                requests.add(request);
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel upload failed", e);
        }
        return bulkCreateProducts(requests);
    }


    private String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case NUMERIC -> new java.text.DecimalFormat("0.##").format(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception e) { yield new java.text.DecimalFormat("0.##").format(cell.getNumericCellValue()); }
            }
            default -> cell.getStringCellValue().trim();
        };
    }

    private BigDecimal getDecimal(Cell cell) {
        return cell == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(cell.getNumericCellValue());
    }

    private int getInt(Cell cell) {
        return cell == null
                ? 0
                : (int) cell.getNumericCellValue();
    }


}
