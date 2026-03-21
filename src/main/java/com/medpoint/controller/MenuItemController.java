package com.medpoint.controller;
import com.medpoint.dto.request.MenuItemRequest;
import com.medpoint.dto.response.MenuItemResponse;
import com.medpoint.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
//
//    private final MenuItemService menuItemService;
//
//    /** GET /api/v1/menu-items — list all active menu items */
//    @GetMapping
//    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
//        return ResponseEntity.ok(menuItemService.getAllActive());
//    }
//
//    /** GET /api/v1/menu-items/{id} */
//    @GetMapping("/{id}")
//    public ResponseEntity<MenuItemResponse> getMenuItem(@PathVariable Long id) {
//        return ResponseEntity.ok(menuItemService.getById(id));
//    }
//
//    /** POST /api/v1/menu-items */
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<MenuItemResponse> createMenuItem(
//            @Valid @RequestBody MenuItemRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(menuItemService.create(request));
//    }
//
//    /** PUT /api/v1/menu-items/{id} */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<MenuItemResponse> updateMenuItem(
//            @PathVariable Long id,
//            @Valid @RequestBody MenuItemRequest request) {
//        return ResponseEntity.ok(menuItemService.update(id, request));
//    }
//
//    /** DELETE /api/v1/menu-items/{id} — soft-deletes by setting active = false */
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
//        menuItemService.delete(id);
//        return ResponseEntity.noContent().build();
//    }





    private final MenuItemService menuItemService;

    /** GET /api/v1/menu-items — list all active menu items */
    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllActive());
    }

    /** GET /api/v1/menu-items/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getById(id));
    }

    /** POST /api/v1/menu-items */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> createMenuItem(
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.create(request));
    }

    /** PUT /api/v1/menu-items/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.update(id, request));
    }

    /** DELETE /api/v1/menu-items/{id} — soft-deletes by setting active = false */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}