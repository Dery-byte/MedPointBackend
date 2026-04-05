package com.medpoint.service;
import com.medpoint.dto.request.*;
import com.medpoint.dto.response.*;
import java.util.List;

public interface HotelService {
    List<RoomCategoryResponse> getAllCategories();
    RoomCategoryResponse createCategory(RoomCategoryRequest req);
    RoomCategoryResponse updateCategoryPrice(Long id, UpdateRoomCategoryRequest req);
    void deleteCategory(Long id);

    List<RoomResponse> getAllRooms();
    RoomResponse addRoom(AddRoomRequest req);
    RoomResponse updateRoom(Long id, UpdateRoomRequest req);
    void deleteRoom(Long id);

    BookingResponse addExtrasToBooking(Long bookingId, AddBookingExtrasRequest req);
    BookingResponse updateBooking(Long bookingId, UpdateBookingRequest req);

    List<RoomExtraResponse> getAllExtras();
    List<BookingResponse> getAllBookings();

    /** Check in using roomId from request body */
    BookingResponse checkIn(CheckInRequest req);

    /** Check out using bookingId from request body */
    TransactionResponse checkOut(CheckOutRequest req, Long staffId);
}
