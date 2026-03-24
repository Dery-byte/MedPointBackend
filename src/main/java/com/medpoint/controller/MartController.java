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





    @PostMapping("/products")
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

    /** POST /api/mart/checkout — process a cart sale */
    @PostMapping("/checkout")
    public ResponseEntity<TransactionResponse> checkout(@Valid @RequestBody MartCheckoutRequest request,
                                                        @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(martService.checkout(request, currentUser.getId()));
    }
}
