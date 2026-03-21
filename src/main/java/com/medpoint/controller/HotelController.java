package com.medpoint.controller;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import com.medpoint.entity.User;
import com.medpoint.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    // ── Room Categories ───────────────────────────────────────────────────────

    @GetMapping("/room-categories")
    public ResponseEntity<List<RoomCategoryResponse>> getCategories() {
        return ResponseEntity.ok(hotelService.getAllCategories());
    }

    @PostMapping("/room-categories")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageHotel(authentication)")
    public ResponseEntity<RoomCategoryResponse> addCategory(@Valid @RequestBody RoomCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.createCategory(req));
    }

    @PutMapping("/room-categories/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageHotel(authentication)")
    public ResponseEntity<RoomCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateRoomCategoryRequest req) {
        return ResponseEntity.ok(hotelService.updateCategoryPrice(id, req));
    }



    @DeleteMapping("/room-categories/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageHotel(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        hotelService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted."));
    }

    // ── Rooms ─────────────────────────────────────────────────────────────────

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomResponse>> getRooms() {
        return ResponseEntity.ok(hotelService.getAllRooms());
    }

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageHotel(authentication)")
    public ResponseEntity<RoomResponse> addRoom(@Valid @RequestBody AddRoomRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.addRoom(req));
    }

    @DeleteMapping("/rooms/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or @permissionGuard.canManageHotel(authentication)")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        hotelService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.ok("Room deleted."));
    }

    // ── Room Extras ───────────────────────────────────────────────────────────

    @GetMapping("/room-extras")
    public ResponseEntity<List<RoomExtraResponse>> getExtras() {
        return ResponseEntity.ok(hotelService.getAllExtras());
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getBookings() {
        return ResponseEntity.ok(hotelService.getAllBookings());
    }

    /**
     * POST /hotel/check-in
     * Frontend CheckInRequest includes roomId (not roomNumber in URL).
     * Returns BookingResponse.
     */
    @PostMapping("/check-in")
    public ResponseEntity<BookingResponse> checkIn(@Valid @RequestBody CheckInRequest req,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.checkIn(req));
    }

    /**
     * POST /hotel/check-out
     * Frontend CheckOutRequest: { bookingId, extraIds[] }
     * Returns TransactionResponse.
     */
    @PostMapping("/check-out")
    public ResponseEntity<TransactionResponse> checkOut(@Valid @RequestBody CheckOutRequest req,
                                                         @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(hotelService.checkOut(req, currentUser.getId()));
    }
}
