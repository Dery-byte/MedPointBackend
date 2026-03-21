package com.medpoint.controller;
import com.medpoint.dto.request.CategoryRequest;
import com.medpoint.dto.response.ApiResponse;
import com.medpoint.dto.response.CategoryResponse;
import com.medpoint.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mart/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** GET /mart/categories — public, used to populate dropdowns */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    /** POST /mart/categories */
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    /** PUT /mart/categories/:id — rename */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<CategoryResponse> rename(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.rename(id, request));
    }

    /** DELETE /mart/categories/:id */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageMart(authentication)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted."));
    }
}