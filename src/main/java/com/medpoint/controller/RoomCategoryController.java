package com.medpoint.controller;
import com.medpoint.dto.request.RoomCategoryRequest;
import com.medpoint.dto.response.RoomCategoryResponse;
import com.medpoint.entity.RoomCategory;
import com.medpoint.repository.RoomCategoryRepository;
import com.medpoint.service.RoomCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/room-categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomCategoryController {
    private final RoomCategoryRepository roomCategoryRepository;

    private final RoomCategoryService roomCategoryService;

//    @GetMapping
//    public ResponseEntity<List<RoomCategory>> getAll() {
//        return ResponseEntity.ok(roomCategoryRepository.findAll());
//    }



//
//    /** GET /api/v1/room-categories — list all categories (used by frontend dropdown) */
//    @GetMapping
//    public ResponseEntity<List<RoomCategoryResponse>> getAllCategories() {
//        return ResponseEntity.ok(roomCategoryService.getAll());
//    }
//
//    /** GET /api/v1/room-categories/{id} */
//    @GetMapping("/{id}")
//    public ResponseEntity<RoomCategoryResponse> getCategory(@PathVariable Long id) {
//        return ResponseEntity.ok(roomCategoryService.getById(id));
//    }
//
//    /** POST /api/v1/room-categories */
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<RoomCategoryResponse> createCategory(
//            @Valid @RequestBody RoomCategoryRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(roomCategoryService.create(request));
//    }
//
//    /** PUT /api/v1/room-categories/{id} */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<RoomCategoryResponse> updateCategory(
//            @PathVariable Long id,
//            @Valid @RequestBody RoomCategoryRequest request) {
//        return ResponseEntity.ok(roomCategoryService.update(id, request));
//    }
//
//    /** DELETE /api/v1/room-categories/{id} */
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
//        roomCategoryService.delete(id);
//        return ResponseEntity.noContent().build();
//    }

    /** GET /api/v1/room-categories — list all categories (used by frontend dropdown) */
    @GetMapping
    public ResponseEntity<List<RoomCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(roomCategoryService.getAll());
    }

    /** GET /api/v1/room-categories/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<RoomCategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(roomCategoryService.getById(id));
    }

    /** POST /api/v1/room-categories */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomCategoryResponse> createCategory(
            @Valid @RequestBody RoomCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomCategoryService.create(request));
    }

    /** PUT /api/v1/room-categories/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody RoomCategoryRequest request) {
        return ResponseEntity.ok(roomCategoryService.update(id, request));
    }

    /** DELETE /api/v1/room-categories/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        roomCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}