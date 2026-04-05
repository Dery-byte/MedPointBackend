package com.medpoint.controller;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.User;
import com.medpoint.service.MartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/mart")
@RequiredArgsConstructor
public class MartController {
    private final MartService martService;


    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(martService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(martService.getProductById(id));
    }

//    @GetMapping("/categories")
//    public ResponseEntity<List<String>> getCategories() {
//        return ResponseEntity.ok(martService.getCategories());
//    }





    @PostMapping("/products") // CHANGED FROM products TO product
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(martService.createProduct(request));
    }







    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(martService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        martService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted."));
    }

    @PatchMapping("/products/{id}/restock")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ProductResponse> restock(@PathVariable Long id,
                                                   @Valid @RequestBody RestockRequest request) {
        return ResponseEntity.ok(martService.restockProduct(id, request));
    }

    @PatchMapping("/products/{id}/price")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ProductResponse> updatePrice(@PathVariable Long id,
                                                       @Valid @RequestBody PriceUpdateRequest request) {
        return ResponseEntity.ok(martService.updateProductPrice(id, request));
    }

    /** POST /api/mart/products/{id}/image — upload product image */
    @PostMapping("/products/{id}/image")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ImageUploadResponse> uploadImage(@PathVariable Long id,
                                                           @RequestParam("image") MultipartFile file) {
        String imageUrl = martService.uploadProductImage(id, file);
        return ResponseEntity.ok(new ImageUploadResponse(imageUrl));
    }










    /** POST /api/mart/products/bulk — bulk create products */
    @PostMapping("/products/bulk")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<List<ProductResponse>> bulkCreate(@Valid @RequestBody BulkProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(martService.bulkCreateProducts(req.getProducts()));
    }

    /** GET /api/mart/categories — list product categories */
    @GetMapping("/categorie") // CHNAGED FROM categories TO categorie
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(martService.getCategories());
    }

    /** POST /api/mart/checkout — process a cart sale */
    @PostMapping("/checkout")
    public ResponseEntity<TransactionResponse> checkout(@Valid @RequestBody MartCheckoutRequest request,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(martService.checkout(request, currentUser.getId()));
    }





    @PostMapping("/products/upload-excel")
    public ResponseEntity<?> uploadExcel(
            @RequestParam("file") MultipartFile file
    ) {
        List<ProductResponse> products =
                martService.uploadProductsFromExcel(file);
        return ResponseEntity.ok(
                products.size() + " products uploaded successfully ✅"
        );
    }
}
