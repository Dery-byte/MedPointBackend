package com.medpoint.controller;
import com.medpoint.dto.request.RoomRequest;
import com.medpoint.dto.response.RoomResponse;
import com.medpoint.entity.Room;
import com.medpoint.entity.RoomCategory;
import com.medpoint.enums.RoomStatus;
import com.medpoint.repository.RoomRepository;
import com.medpoint.repository.RoomCategoryRepository;
import com.medpoint.service.RoomService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
private final RoomService roomService;
    // ── DTOs ──────────────────────────────────────────────────────────────────
//
//    @Data
//    public static class RoomRequest {
//        @NotBlank(message = "Room number is required")
//        private String roomNumber;
//
//        @NotNull(message = "Category ID is required")
//        private Long categoryId;
//
//        private RoomStatus status = RoomStatus.AVAILABLE;
//    }
//
//    @Data
//    public static class ErrorResponse {
//        private final String message;
//    }
//
//    // ── GET all rooms ─────────────────────────────────────────────────────────
//
//    @GetMapping
//    public ResponseEntity<List<Room>> getAllRooms() {
//        return ResponseEntity.ok(roomRepository.findAllByOrderByRoomNumberAsc());
//    }
//
//    // ── GET single room ───────────────────────────────────────────────────────
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getRoom(@PathVariable Long id) {
//        return roomRepository.findById(id)
//                .<ResponseEntity<?>>map(ResponseEntity::ok)
//                .orElse(ResponseEntity.status(404)
//                        .body(new ErrorResponse("Room not found")));
//    }
//
//    // ── GET rooms by status ───────────────────────────────────────────────────
//
//    @GetMapping("/status/{status}")
//    public ResponseEntity<List<Room>> getRoomsByStatus(@PathVariable RoomStatus status) {
//        return ResponseEntity.ok(roomRepository.findByStatus(status));
//    }
//
//    // ── POST create room ──────────────────────────────────────────────────────
//
//    @PostMapping
//    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest req) {
//        if (roomRepository.existsByRoomNumber(req.getRoomNumber())) {
//            return ResponseEntity.badRequest()
//                    .body(new ErrorResponse("Room number '" + req.getRoomNumber() + "' already exists"));
//        }
//
//        RoomCategory category = roomCategoryRepository.findById(req.getCategoryId())
//                .orElse(null);
//        if (category == null) {
//            return ResponseEntity.badRequest()
//                    .body(new ErrorResponse("Category not found"));
//        }
//
//        Room room = Room.builder()
//                .roomNumber(req.getRoomNumber())
//                .category(category)
//                .status(req.getStatus() != null ? req.getStatus() : RoomStatus.AVAILABLE)
//                .build();
//
//        return ResponseEntity.status(201).body(roomRepository.save(room));
//    }
//
//    // ── PUT update room ───────────────────────────────────────────────────────
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateRoom(@PathVariable Long id,
//                                        @Valid @RequestBody RoomRequest req) {
//        Room room = roomRepository.findById(id).orElse(null);
//        if (room == null) {
//            return ResponseEntity.status(404)
//                    .body(new ErrorResponse("Room not found"));
//        }
//
//        // Check duplicate room number (excluding self)
//        roomRepository.findByRoomNumber(req.getRoomNumber())
//                .filter(existing -> !existing.getId().equals(id))
//                .ifPresent(dup -> {
//                    throw new IllegalArgumentException(
//                            "Room number '" + req.getRoomNumber() + "' already exists");
//                });
//
//        RoomCategory category = roomCategoryRepository.findById(req.getCategoryId())
//                .orElse(null);
//        if (category == null) {
//            return ResponseEntity.badRequest()
//                    .body(new ErrorResponse("Category not found"));
//        }
//
//        room.setRoomNumber(req.getRoomNumber());
//        room.setCategory(category);
//        room.setStatus(req.getStatus() != null ? req.getStatus() : room.getStatus());
//
//        return ResponseEntity.ok(roomRepository.save(room));
//    }
//
//    // ── DELETE room ───────────────────────────────────────────────────────────
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
//        if (!roomRepository.existsById(id)) {
//            return ResponseEntity.status(404)
//                    .body(new ErrorResponse("Room not found"));
//        }
//        roomRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }







//
//    /** GET /api/v1/rooms — list all rooms with nested category */
//    @GetMapping
//    public ResponseEntity<List<RoomResponse>> getAllRooms() {
//        return ResponseEntity.ok(roomService.getAll());
//    }
//
//    /** GET /api/v1/rooms/{id} */
//    @GetMapping("/{id}")
//    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
//        return ResponseEntity.ok(roomService.getById(id));
//    }
//
//    /** POST /api/v1/rooms */
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<RoomResponse> createRoom(
//            @Valid @RequestBody RoomRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(roomService.create(request));
//    }
//
//    /** PUT /api/v1/rooms/{id} */
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<RoomResponse> updateRoom(
//            @PathVariable Long id,
//            @Valid @RequestBody RoomRequest request) {
//        return ResponseEntity.ok(roomService.update(id, request));
//    }
//
//    /** DELETE /api/v1/rooms/{id} */
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
//        roomService.delete(id);
//        return ResponseEntity.noContent().build();
//    }





    /** GET /api/v1/rooms — list all rooms with nested category */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAll());
    }

    /** GET /api/v1/rooms/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getById(id));
    }

    /** POST /api/v1/rooms */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.create(request));
    }

    /** PUT /api/v1/rooms/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.update(id, request));
    }

    /** DELETE /api/v1/rooms/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}